package com.github.gikolipse.views;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.SWT;
import org.eclipse.core.runtime.IAdaptable;

/**
 * This sample class demonstrates how to plug-in a new
 * workbench view. The view shows data obtained from the
 * model. The sample creates a dummy model on the fly,
 * but a real implementation would connect to the model
 * available either in this or another plug-in (e.g. the workspace).
 * The view is connected to the model using a content provider.
 * <p>
 * The view uses a label provider to define how model
 * objects should be presented in the view. Each
 * view can present the same model objects using
 * different labels and icons, if needed. Alternatively,
 * a single label provider can be shared between views
 * in order to ensure that objects of the same type are
 * presented in the same way everywhere.
 * <p>
 */

public class BBSListView extends ViewPart
{
	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.github.gikolipse.views.BBSListView";

	private TreeViewer viewer;
	private DrillDownAdapter drillDownAdapter;
	private Action action1;
	private Action action2;
	private Action doubleClickAction;

	/*
	 * The content provider class is responsible for
	 * providing objects to the view. It can wrap
	 * existing objects in adapters or simply return
	 * objects as-is. These objects may be sensitive
	 * to the current input of the view, or ignore
	 * it and always show the same content 
	 * (like Task List, for example).
	 */

	class TreeObject implements IAdaptable
	{
		private String name;
		private TreeParent parent;

		public TreeObject(String name)
		{
			this.name = name;
		}

		public String getName()
		{
			return name;
		}

		public void setParent(TreeParent parent)
		{
			this.parent = parent;
		}

		public TreeParent getParent()
		{
			return parent;
		}

		public String toString()
		{
			return getName();
		}

		public Object getAdapter(Class key)
		{
			return null;
		}
	}

	class TreeParent extends TreeObject
	{
		private ArrayList children;

		public TreeParent(String name)
		{
			super(name);
			children = new ArrayList();
		}

		public void addChild(TreeObject child)
		{
			children.add(child);
			child.setParent(this);
		}

		public void removeChild(TreeObject child)
		{
			children.remove(child);
			child.setParent(null);
		}

		public TreeObject[] getChildren()
		{
			return (TreeObject[]) children.toArray(new TreeObject[children.size()]);
		}

		public boolean hasChildren()
		{
			return children.size() > 0;
		}
	}

	class ViewContentProvider implements IStructuredContentProvider, ITreeContentProvider
	{
		private TreeParent invisibleRoot;

		public void inputChanged(Viewer v, Object oldInput, Object newInput)
		{
		}

		public void dispose()
		{
		}

		public Object[] getElements(Object parent)
		{
			if (parent.equals(getViewSite()))
			{
				if (invisibleRoot == null) initialize();
				return getChildren(invisibleRoot);
			}
			return getChildren(parent);
		}

		public Object getParent(Object child)
		{
			if (child instanceof TreeObject)
			{
				return ((TreeObject) child).getParent();
			}
			return null;
		}

		public Object[] getChildren(Object parent)
		{
			if (parent instanceof TreeParent)
			{
				return ((TreeParent) parent).getChildren();
			}
			return new Object[0];
		}

		public boolean hasChildren(Object parent)
		{
			if (parent instanceof TreeParent) return ((TreeParent) parent).hasChildren();
			return false;
		}

		/*
		 * We will set up a dummy model to initialize tree heararchy.
		 * In a real code, you will connect to a real model and
		 * expose its hierarchy.
		 */
		private void initialize()
		{

			Map<String, List<A>> categoryMap = new HashMap<String, List<A>>();

			BufferedReader br = null;
			try
			{
				URL url = new URL("http://menu.2ch.net/bbsmenu.html");

				URLConnection uc = url.openConnection();
				BufferedInputStream bis = new BufferedInputStream(uc.getInputStream());
				br = new BufferedReader(new InputStreamReader(bis, "Shift_JIS"));
				String line;
				boolean categoryFlag = false;
				String category = "";

				Pattern categoryPattern = Pattern.compile("^<BR><BR><B>.*</B><BR>$");
				Pattern linkPattern = Pattern.compile("^<A HREF=.*");

				while ((line = br.readLine()) != null)
				{
					String linkText = "";
					String linkUrl = "";

					if (line.equals(""))
					{
						categoryFlag = false;
					}

					Matcher categoryPatternMatcher = categoryPattern.matcher(line);
					if (categoryPatternMatcher.matches())
					{
						categoryFlag = true;

						Pattern pattern = Pattern.compile("<.+?>", Pattern.CASE_INSENSITIVE);
						Matcher matcher = pattern.matcher(line);
						category = matcher.replaceAll("");
						category = category.trim();

						if (!categoryMap.containsKey(category))
						{
							categoryMap.put(category, new ArrayList<A>());
						}
					}

					Matcher linkPatternMatcher = linkPattern.matcher(line);
					if (categoryFlag && linkPatternMatcher.matches())
					{
						Pattern pattern = Pattern.compile("<.+?>", Pattern.CASE_INSENSITIVE);
						Matcher matcher = pattern.matcher(line);
						linkText = matcher.replaceAll("");

						linkUrl = line.substring(line.indexOf("http"), line.indexOf(">"));
						linkUrl = linkUrl.replaceAll(" TARGET=_blank", "");

						// 追加
						List<A> aList = categoryMap.get(category);
						aList.add(new A(linkText, linkUrl));
						categoryMap.put(category, aList);
					}

				}
			} catch (MalformedURLException ex)
			{
				ex.printStackTrace();
			} catch (UnknownHostException ex)
			{
				ex.printStackTrace();
			} catch (IOException ex)
			{
				ex.printStackTrace();
			} catch (Exception ex)
			{

				ex.printStackTrace();
			} finally
			{
				try
				{
					br.close();
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}

			TreeParent root = new TreeParent("一覧");
			Set<String> categoryKeySet = categoryMap.keySet();
			for (String category : categoryKeySet)
			{
				TreeParent categoryNode = new TreeParent(category);
				List<A> aList = categoryMap.get(category);
				for (A a : aList)
				{
					TreeObject leaf = new TreeObject(a.text);
					categoryNode.addChild(leaf);
				}

				root.addChild(categoryNode);
			}

			invisibleRoot = new TreeParent("");
			invisibleRoot.addChild(root);
		}
	}

	class A
	{
		public A(String text, String url)
		{
			this.text = text;
			this.url = url;
		}

		public String text;
		public String url;
	}

	class ViewLabelProvider extends LabelProvider
	{

		public String getText(Object obj)
		{
			return obj.toString();
		}

		public Image getImage(Object obj)
		{
			String imageKey = ISharedImages.IMG_OBJ_ELEMENT;
			if (obj instanceof TreeParent) imageKey = ISharedImages.IMG_OBJ_FOLDER;
			return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
		}
	}

	class NameSorter extends ViewerSorter
	{
	}

	/**
	 * The constructor.
	 */
	public BBSListView()
	{
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent)
	{
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		drillDownAdapter = new DrillDownAdapter(viewer);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new NameSorter());
		viewer.setInput(getViewSite());

		// Create the help context id for the viewer's control
		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "Gikolipse.viewer");
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
	}

	private void hookContextMenu()
	{
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener()
		{
			public void menuAboutToShow(IMenuManager manager)
			{
				BBSListView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars()
	{
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager)
	{
		manager.add(action1);
		manager.add(new Separator());
		manager.add(action2);
	}

	private void fillContextMenu(IMenuManager manager)
	{
		manager.add(action1);
		manager.add(action2);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager)
	{
		manager.add(action1);
		manager.add(action2);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
	}

	private void makeActions()
	{
		action1 = new Action()
		{
			public void run()
			{
				showMessage("Action 1 executed");
			}
		};
		action1.setText("Action 1");
		action1.setToolTipText("Action 1 tooltip");
		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));

		action2 = new Action()
		{
			public void run()
			{
				showMessage("Action 2 executed");
			}
		};
		action2.setText("Action 2");
		action2.setToolTipText("Action 2 tooltip");
		action2.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		doubleClickAction = new Action()
		{
			public void run()
			{
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection) selection).getFirstElement();
				showMessage("Double-click detected on " + obj.toString());
			}
		};
	}

	private void hookDoubleClickAction()
	{
		viewer.addDoubleClickListener(new IDoubleClickListener()
		{
			public void doubleClick(DoubleClickEvent event)
			{
				doubleClickAction.run();
			}
		});
	}

	private void showMessage(String message)
	{
		MessageDialog.openInformation(viewer.getControl().getShell(), "BBSList", message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus()
	{
		viewer.getControl().setFocus();
	}
}