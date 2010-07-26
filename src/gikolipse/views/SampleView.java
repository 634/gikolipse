package gikolipse.views;

import static com.github.gikolipse.utils.Const.ENCODING_2CH;
import static com.github.gikolipse.utils.Const.RETURN_STRING;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;

import com.github.gikolipse.utils.A;
import com.github.gikolipse.utils.WebUtil;

/**
 * This sample class demonstrates how to plug-in a new workbench view. The view
 * shows data obtained from the model. The sample creates a dummy model on the
 * fly, but a real implementation would connect to the model available either in
 * this or another plug-in (e.g. the workspace). The view is connected to the
 * model using a content provider.
 * <p>
 * The view uses a label provider to define how model objects should be
 * presented in the view. Each view can present the same model objects using
 * different labels and icons, if needed. Alternatively, a single label provider
 * can be shared between views in order to ensure that objects of the same type
 * are presented in the same way everywhere.
 * <p>
 */

public class SampleView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "gikolipse.views.SampleView";

	/*
	 * The content provider class is responsible for providing objects to the
	 * view. It can wrap existing objects in adapters or simply return objects
	 * as-is. These objects may be sensitive to the current input of the view,
	 * or ignore it and always show the same content (like Task List, for
	 * example).
	 */

	class ViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		public Object[] getElements(Object parent) {
			WebUtil webUtil = new WebUtil();
			String html = webUtil.readContents("http://society6.2ch.net/kokusai/subback.html", ENCODING_2CH);

			String[] htmlLines = html.split(RETURN_STRING);

			// 1278602620/l50
			Pattern linkPattern = Pattern.compile("^<a href=\".*");

			List<A> aList = new ArrayList<A>();
			for (String htmlLine : htmlLines) {
				Matcher categoryPatternMatcher = linkPattern.matcher(htmlLine);
				if (categoryPatternMatcher.matches()) {
					Pattern pattern = Pattern.compile("<.+?>", Pattern.CASE_INSENSITIVE);
					Matcher matcher = pattern.matcher(htmlLine);
					String linkText = matcher.replaceAll("");

					String linkUrl = htmlLine.substring(htmlLine.indexOf("href=\"") + 6, htmlLine.indexOf(">"));

					aList.add(new A(linkText, linkUrl));
				}
			}

			A[] aArray = aList.toArray(new A[] {});
			return aArray;
		}
	}

	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		// Cellに表示するイメージを返却する
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		// Cellに表示する値を返却する
		public String getColumnText(Object element, int columnIndex) {
			A customer = (A) element;
			switch (columnIndex) {
			case 0:
				return customer.text;
			case 1:
				return "2";
			case 2:
				return "3";
			}
			return "8";
		}
	}

	class NameSorter extends ViewerSorter {
	}

	/**
	 * The constructor.
	 */
	public SampleView() {
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {

		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new FillLayout());
		TableViewer viewer = new TableViewer(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		TableColumn column1 = new TableColumn(table, SWT.NULL);
		column1.setText("名前");
		column1.setWidth(200);

		TableColumn column2 = new TableColumn(table, SWT.NULL);
		column2.setText("住所");
		column2.setWidth(400);

		TableColumn column3 = new TableColumn(table, SWT.NULL);
		column3.setText("電話番号");
		column3.setWidth(200);

		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());

		viewer.setInput(getItems());
	}

	private List<A> getItems() {

		List<A> list = new ArrayList<A>();
		A model1 = new A("a", "b");
		list.add(model1);
		A model2 = new A("c", "d");
		list.add(model2);
		A model3 = new A("e", "f");
		list.add(model3);

		return list;
	}

	public void setFocus() {
	}
}