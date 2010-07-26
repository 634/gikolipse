package com.github.gikolipse.views;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.action.Action;
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
import org.eclipse.ui.part.ViewPart;

import com.github.gikolipse.services.BBSListService;
import com.github.gikolipse.utils.A;

public class BBSListView extends ViewPart {

	private Action topClickAction;
	private Action categoryClickAction;
	private Action threadClickAction;
	private TableColumn column;
	private TableViewer viewer;
	private IDoubleClickListener topDoubleClickListener;
	private IDoubleClickListener categoryDoubleClickListener;
	private IDoubleClickListener threadDoubleClickListener;

	public static final String ID = "gikolipse.views.SampleView";

	private void createTopControl(Composite parent) {
		Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(false);

		column = new TableColumn(table, SWT.NULL, 0);
		column.setText("-");
		column.setWidth(200);

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
		viewer.addDoubleClickListener(topDoubleClickListener);
	}

	private void createCategoryControl(String topCategoryString) {
		Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(false);

		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());

		BBSListService bbsListService = new BBSListService();
		Map<String, List<A>> categoryMap = bbsListService.createBBSList();
		List<A> bbsList = categoryMap.get(topCategoryString);

		viewer.setInput(bbsList);

		viewer.removeDoubleClickListener(topDoubleClickListener);
		viewer.addDoubleClickListener(categoryDoubleClickListener);
	}

	private void createThreadListControl(A a) {
		Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(false);

		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());

		BBSListService bbsListService = new BBSListService();
		List<A> threadList = bbsListService.createThreadList(a.url);
		viewer.setInput(threadList);

		viewer.removeDoubleClickListener(categoryDoubleClickListener);
		viewer.addDoubleClickListener(threadDoubleClickListener);
	}

	private void createThreadViewControl(A a) {
		System.out.println(a.url);
		System.out.println("");
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

	private void threadClickAction() {
		ISelection selection = viewer.getSelection();
		Object obj = ((IStructuredSelection) selection).getFirstElement();
		A a = (A) obj;

		createThreadViewControl(a);
	}

	private void initialize() {
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

	public void createPartControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new FillLayout());
		viewer = new TableViewer(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);

		initialize();

		createTopControl(parent);
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