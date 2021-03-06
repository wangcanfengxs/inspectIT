package rocks.inspectit.ui.rcp.handlers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import rocks.inspectit.shared.all.communication.data.HttpTimerData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceData;
import rocks.inspectit.shared.all.communication.data.InvocationSequenceDataHelper;
import rocks.inspectit.shared.all.communication.data.ParameterContentData;
import rocks.inspectit.ui.rcp.InspectIT;
import rocks.inspectit.ui.rcp.formatter.TextFormatter;
import rocks.inspectit.ui.rcp.util.ClipboardUtil;

/**
 * Handler for copying context parameters.
 *
 * @author Ivan Senic
 *
 */
public class CopyContextParametersHandler extends AbstractHandler implements IHandler {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		StructuredSelection selection = (StructuredSelection) HandlerUtil.getCurrentSelection(event);
		if (selection.isEmpty()) {
			return null;
		}

		StringBuilder stringBuilder = new StringBuilder();
		for (Object selected : selection.toArray()) {
			if (selected instanceof InvocationSequenceData) {
				InvocationSequenceData invocation = (InvocationSequenceData) selected;

				boolean lineAdded = false;
				if (InvocationSequenceDataHelper.hasHttpTimerData(invocation)) {
					HttpTimerData httpTimer = (HttpTimerData) invocation.getTimerData();
					if (null != httpTimer.getHttpInfo().getUri()) {
						stringBuilder.append("URI: ");
						stringBuilder.append(httpTimer.getHttpInfo().getUri());
						stringBuilder.append('\t');
						lineAdded = true;
					}
				}

				if (InvocationSequenceDataHelper.hasCapturedParameters(invocation)) {
					List<ParameterContentData> parameters = new ArrayList<>(InvocationSequenceDataHelper.getCapturedParameters(invocation, true));

					for (ParameterContentData parameterContentData : parameters) {
						stringBuilder.append('\'');
						stringBuilder.append(parameterContentData.getName());
						stringBuilder.append("': ");
						stringBuilder.append(TextFormatter.clearLineBreaks(parameterContentData.getContent()));
						stringBuilder.append('\t');
						lineAdded = true;
					}
				}

				if (lineAdded) {
					stringBuilder.append('\n');
				}

			}
		}

		if (stringBuilder.length() > 0) {
			ClipboardUtil.textToClipboard(HandlerUtil.getActiveShell(event).getDisplay(), stringBuilder.toString());
		} else {
			InspectIT.getDefault().createInfoDialog("No context parameter(s) available in selected data.", -1);
		}
		return null;
	}

}
