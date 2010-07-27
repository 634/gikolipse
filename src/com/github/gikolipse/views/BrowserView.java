package com.github.gikolipse.views;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.github.gikolipse.utils.A;

public class BrowserView extends ViewPart {

    public static final String ID = "com.github.gikolipse.views.BrowserView";

    public BrowserView() {
	super();
    }

    public void createPartControl(Composite parent) {
	IWorkbench workbench = PlatformUI.getWorkbench();
	IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
	IWorkbenchPage page = window.getActivePage();

	ISelection selection = page.getSelection(ThreadListView.ID);
	Object obj = ((IStructuredSelection) selection).getFirstElement();
	A threadUrl = (A) obj;

	Browser browser = new Browser(parent, SWT.NONE);
	browser.setUrl(threadUrl.url);
    }

    public void setFocus() {
    }

}