package com.github.gikolipse.views;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.github.gikolipse.exceptions.GikolipseException;
import com.github.gikolipse.services.BBSService;
import com.github.gikolipse.utils.A;

public class ThreadListView extends ViewPart {

	public static final String ID = "com.github.gikolipse.views.ThreadListView";

	// ホーム→カテゴリ
	private Action topClickAction;

	// カテゴリ→スレッド
	private Action categoryClickAction;

	// スレッドクリック
	private Action threadClickAction;

	// *→ホーム
	private Action goHomeAction;

	// スレッド→カテゴリ
	private Action goCategoryAction;

	// 再読み込み
	private Action refreshAction;

	// スレッド→ブラウザビュー
	private Action threadClickBrowserAction;

	private TableViewer viewer;

	private IDoubleClickListener topDoubleClickListener;
	private IDoubleClickListener categoryDoubleClickListener;
	private IDoubleClickListener threadDoubleClickListener;

	private String backCategory;
	private String currentCategory;
	private A currentThread;

	@Override
	public void createPartControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new FillLayout());
		viewer = new TableViewer(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);

		initialize();

		createTopControl();
	}

	/**
	 * 共通初期化処理
	 */
	private void initialize() {
		getSite().setSelectionProvider(viewer);

		Table table = viewer.getTable();
		table.setHeaderVisible(false);
		table.setLinesVisible(false);

		TableColumn column = new TableColumn(table, SWT.NULL, 0);
		column.setText("-");
		column.setWidth(800);

		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());

		topClickAction = new Action() {
			public void run() {
				topClickAction();
			}
		};

		categoryClickAction = new Action() {
			public void run() {
				categoryClickAction();
			}
		};

		threadClickAction = new Action() {
			public void run() {
				threadClickAction();
			}
		};

		threadClickBrowserAction = new Action() {
			public void run() {
				threadClickBrowserAction();
			}
		};

		goHomeAction = new Action() {
			public void run() {
				goHomeAction();
			}
		};

		goCategoryAction = new Action() {
			public void run() {
				goCategoryAction();
			}
		};

		refreshAction = new Action() {
			public void run() {
				if (currentThread != null) {
					createThreadListControl(currentThread);
				} else if (currentCategory != null) {
					createCategoryControl(currentCategory);
				} else {
					createTopControl();
				}
			}
		};

		goHomeAction.setText("ホーム");
		goHomeAction.setToolTipText("ホームに戻る");
		goHomeAction.setImageDescriptor(ImageDescriptor.createFromFile(getClass(), "/icons/icon_home.gif"));
		IActionBars bars = getViewSite().getActionBars();
		IToolBarManager toolBarManager = bars.getToolBarManager();
		toolBarManager.add(goHomeAction);

		goCategoryAction.setText("戻る");
		goCategoryAction.setToolTipText("１つ前の画面に戻る");
		goCategoryAction.setImageDescriptor(ImageDescriptor.createFromFile(getClass(), "/icons/action_back.gif"));
		toolBarManager.add(goCategoryAction);

		refreshAction.setText("再読み込み");
		refreshAction.setToolTipText("再読み込み");
		refreshAction.setImageDescriptor(ImageDescriptor.createFromFile(getClass(), "/icons/action_refresh.gif"));
		toolBarManager.add(refreshAction);

		threadClickAction.setText("テキスト形式で閲覧");
		threadClickAction.setToolTipText("テキスト形式で閲覧");
		threadClickAction.setImageDescriptor(ImageDescriptor.createFromFile(getClass(), "/icons/page_text.gif"));
		threadClickAction.setDisabledImageDescriptor(ImageDescriptor.createFromFile(getClass(), "/icons/page_text_disabled.gif"));
		toolBarManager.add(threadClickAction);

		threadClickBrowserAction.setText("ブラウザビューで閲覧");
		threadClickBrowserAction.setToolTipText("ブラウザビューで閲覧");
		threadClickBrowserAction.setImageDescriptor(ImageDescriptor.createFromFile(getClass(), "/icons/page_url.gif"));
		threadClickBrowserAction.setDisabledImageDescriptor(ImageDescriptor.createFromFile(getClass(), "/icons/page_url_disabled.gif"));
		toolBarManager.add(threadClickBrowserAction);

		threadClickAction.setEnabled(false);
		threadClickBrowserAction.setEnabled(false);

		topDoubleClickListener = new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				topClickAction.run();
			}
		};
		categoryDoubleClickListener = new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				categoryClickAction.run();
			}
		};
		threadDoubleClickListener = new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				threadClickAction.run();
			}
		};
	}

	private void setDoubldClickListener(IDoubleClickListener doubleClickListener) {
		removeAllDoubleClickListener();
		viewer.addDoubleClickListener(doubleClickListener);
	}

	private void removeAllDoubleClickListener() {
		viewer.removeDoubleClickListener(topDoubleClickListener);
		viewer.removeDoubleClickListener(categoryDoubleClickListener);
		viewer.removeDoubleClickListener(threadDoubleClickListener);
	}

	private void createTopControl() {
		currentCategory = null;
		backCategory = null;
		currentThread = null;

		BBSService bbsService = new BBSService();
		Map<String, List<A>> categoryMap = bbsService.createBBSList();

		Set<String> categoryKeySet = categoryMap.keySet();
		List<String> topList = new ArrayList<String>();
		for (String category : categoryKeySet) {
			topList.add(category);
		}

		viewer.setInput(topList);
		setDoubldClickListener(topDoubleClickListener);
	}

	private void createCategoryControl(String topCategoryString) {
		currentCategory = topCategoryString;
		backCategory = null;
		currentThread = null;

		BBSService bbsListService = new BBSService();
		Map<String, List<A>> categoryMap = bbsListService.createBBSList();
		List<A> bbsList = categoryMap.get(topCategoryString);

		viewer.setInput(bbsList);

		setDoubldClickListener(categoryDoubleClickListener);
	}

	private void createThreadListControl(A a) {
		backCategory = currentCategory;
		currentThread = a;

		BBSService bbsListService = new BBSService();
		List<A> threadList = bbsListService.createThreadList(a.url);
		viewer.setInput(threadList);

		threadClickAction.setEnabled(true);
		threadClickBrowserAction.setEnabled(true);

		setDoubldClickListener(threadDoubleClickListener);
	}

	private void createThreadTextViewControl(A a) {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		try {
			page.showView(ThreadTextView.ID);
		} catch (PartInitException e) {
			throw new GikolipseException(e);
		}
	}

	private void createThreadViewBrowserControl(A a) {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		try {
			page.showView(ThreadBrowserView.ID);
		} catch (PartInitException e) {
			throw new GikolipseException(e);
		}
	}

	private void topClickAction() {
		ISelection selection = viewer.getSelection();
		Object obj = ((IStructuredSelection) selection).getFirstElement();
		String topCategoryString = obj.toString();

		createCategoryControl(topCategoryString);
	}

	private void categoryClickAction() {
		ISelection selection = viewer.getSelection();
		Object obj = ((IStructuredSelection) selection).getFirstElement();
		A a = (A) obj;

		createThreadListControl(a);
	}

	private void goHomeAction() {
		threadClickAction.setEnabled(false);
		threadClickBrowserAction.setEnabled(false);

		createTopControl();
	}

	private void goCategoryAction() {
		threadClickAction.setEnabled(false);
		threadClickBrowserAction.setEnabled(false);

		if (backCategory != null) {
			createCategoryControl(currentCategory);
		} else if (currentCategory != null) {
			createTopControl();
		}
	}

	private void threadClickAction() {
		ISelection selection = viewer.getSelection();
		Object obj = ((IStructuredSelection) selection).getFirstElement();
		A a = (A) obj;

		createThreadTextViewControl(a);
	}

	private void threadClickBrowserAction() {
		ISelection selection = viewer.getSelection();
		Object obj = ((IStructuredSelection) selection).getFirstElement();
		A a = (A) obj;

		createThreadViewBrowserControl(a);
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			return element.toString();
		}
	}
}