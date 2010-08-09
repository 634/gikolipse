package com.github.gikolipse.views;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.github.gikolipse.exceptions.GikolipseException;
import com.github.gikolipse.services.BBSService;
import com.github.gikolipse.utils.A;

public class ThreadTextView extends ViewPart {

	public static final String ID = "com.github.gikolipse.views.ThreadTextView";

	private Text text;

	public ThreadTextView() {
		super();
	}

	@Override
	public void createPartControl(Composite parent) {
		text = new Text(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.READ_ONLY | SWT.WRAP);

		initialize();
	}

	private void initialize() {
		text.setBackground(new Color(Display.getCurrent(), 255, 255, 255));

		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		if(page == null){
			text.setText("表示エラーが発生しました。スレッドを選択してください。");
			return;
		}

		ISelection selection = page.getSelection(ThreadListView.ID);

		if (selection.isEmpty()) {
			text.setText("表示エラーが発生しました。スレッドを選択してください。");
			return;
		}

		Object obj = ((IStructuredSelection) selection).getFirstElement();
		if (obj == null) {
			text.setText("表示エラーが発生しました。スレッドを選択してください。");
			return;
		}

		A threadUrl = (A) obj;
		if(threadUrl == null){
			throw new GikolipseException("パラメータが不正です。");
		}
		
		BBSService service = new BBSService();
		String htmlString = service.getHtmlString(threadUrl);
		
		text.setText(htmlString);
	}
	
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
		
	}
}