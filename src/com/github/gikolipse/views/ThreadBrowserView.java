package com.github.gikolipse.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.github.gikolipse.exceptions.GikolipseException;
import com.github.gikolipse.utils.A;

public class ThreadBrowserView extends ViewPart {

	public static final String ID = "com.github.gikolipse.views.ThreadBrowserView";

	private final String VIEW_FONT = "MS UI Gothic";

	private Browser browser;

	// *→ホーム
	private Action goHomeAction;

	public ThreadBrowserView() {
		super();
	}

	public void createPartControl(Composite parent) {
		browser = new Browser(parent, SWT.NONE);

		initialize();
	}

	private void initialize() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();

		ISelection selection = page.getSelection(ThreadListView.ID);

		if (selection.isEmpty()) {
			browser.setText("表示エラーが発生しました。");
			return;
		}

		Object obj = ((IStructuredSelection) selection).getFirstElement();
		if (obj == null) {
			browser.setText("表示エラーが発生しました。");
			return;
		}

		A threadUrl = (A) obj;

		browser.setUrl(threadUrl.url);

		// フォント指定
		FontData[] fontDataList = Display.getCurrent().getFontList(null, true);
		for (FontData fontData : fontDataList) {
			if (fontData.getName().equals(VIEW_FONT)) {
				browser.setFont(new Font(Display.getDefault(), VIEW_FONT, 9, SWT.NORMAL));
				break;
			}
		}

		goHomeAction = new Action() {
			public void run() {
				goHomeAction();
			}
		};
		goHomeAction.setText("ホーム");
		goHomeAction.setToolTipText("ホームに戻る");
		goHomeAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_REDO));
		IActionBars bars = getViewSite().getActionBars();
		IToolBarManager toolBarManager = bars.getToolBarManager();
		toolBarManager.add(goHomeAction);

	}

	private void goHomeAction() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		try {
			page.showView(ThreadListView.ID);
		} catch (PartInitException e) {
			throw new GikolipseException(e);
		}
	}
	
	@Override
	public void setFocus() {
	}

}