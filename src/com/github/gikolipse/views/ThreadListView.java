package com.github.gikolipse.views;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
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
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.github.gikolipse.exceptions.GikolipseException;
import com.github.gikolipse.services.BBSListService;
import com.github.gikolipse.utils.A;

public class ThreadListView extends ViewPart {

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

    private TableViewer viewer;

    private IDoubleClickListener topDoubleClickListener;
    private IDoubleClickListener categoryDoubleClickListener;
    private IDoubleClickListener threadDoubleClickListener;

    private String backCategory;
    private String currentCategory;

    public static final String ID = "com.github.gikolipse.views.ThreadListView";

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
	Table table = viewer.getTable();
	table.setHeaderVisible(false);
	table.setLinesVisible(false);

	TableColumn column = new TableColumn(table, SWT.NULL, 0);
	column.setText("-");
	column.setWidth(200);

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

	goHomeAction = new Action() {
	    public void run() {
		goHomeAction();
	    }
	};
	goHomeAction.setText("ホーム");
	goHomeAction.setToolTipText("ホームに戻る");
	goHomeAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_ETOOL_HOME_NAV));
	IActionBars bars = getViewSite().getActionBars();
	IToolBarManager toolBarManager = bars.getToolBarManager();
	toolBarManager.add(goHomeAction);

	goCategoryAction = new Action() {
	    public void run() {
		goCategoryAction();
	    }
	};
	goCategoryAction.setText("戻る");
	goCategoryAction.setToolTipText("１つ前の画面に戻る");
	goCategoryAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_BACK));
	toolBarManager.add(goCategoryAction);

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

	viewer.setContentProvider(new ArrayContentProvider());
	viewer.setLabelProvider(new ViewLabelProvider());

	BBSListService bbsListService = new BBSListService();
	Map<String, List<A>> categoryMap = bbsListService.createBBSList();

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
	viewer.setContentProvider(new ArrayContentProvider());
	viewer.setLabelProvider(new ViewLabelProvider());

	BBSListService bbsListService = new BBSListService();
	Map<String, List<A>> categoryMap = bbsListService.createBBSList();
	List<A> bbsList = categoryMap.get(topCategoryString);

	viewer.setInput(bbsList);

	setDoubldClickListener(categoryDoubleClickListener);
    }

    private void createThreadListControl(A a) {
	backCategory = currentCategory;

	viewer.setContentProvider(new ArrayContentProvider());
	viewer.setLabelProvider(new ViewLabelProvider());

	BBSListService bbsListService = new BBSListService();
	List<A> threadList = bbsListService.createThreadList(a.url);
	viewer.setInput(threadList);

	setDoubldClickListener(threadDoubleClickListener);
    }

    private void createThreadViewControl(A a) {
	IWorkbench workbench = PlatformUI.getWorkbench();
	IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
	IWorkbenchPage page = window.getActivePage();
	try {
	    page.showView(ThreadView.ID);
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
	createTopControl();
    }

    private void goCategoryAction() {
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

	createThreadViewControl(a);
    }

    public void setFocus() {
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