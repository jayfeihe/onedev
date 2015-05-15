/*
 * Copyright PMEase Inc.,
 * Date: 2008-8-4
 * Time: ����09:00:25
 * All rights reserved.
 *
 * Revision: $Id$
 */
package com.pmease.commons.wicket.component.wizard;

import java.util.List;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.request.resource.CssResourceReference;

import com.google.common.base.Preconditions;
import com.pmease.commons.wicket.component.feedback.FeedbackPanel;

@SuppressWarnings("serial")
public abstract class Wizard extends Panel {

	private static final String STEP_CONTENT_ID = "content";
	
	private List<? extends WizardStep> steps;
	
	private int activeStepIndex;
	
	public Wizard(String id, List<? extends WizardStep> steps) {
		super(id);
		
		Preconditions.checkArgument(steps != null && !steps.isEmpty());
		this.steps = steps;
	}		

	@Override
	protected void onInitialize() {
		super.onInitialize();

		final Form<?> form = new Form<Void>("form");
		form.add(new Label("title", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				String template = "Step %s of %s: %s";
				return String.format(template, activeStepIndex+1, steps.size(), getActiveStep().getMessage());
			}
			
		}));
		
		form.add(new FeedbackPanel("feedback", form));
		form.add(getActiveStep().render(STEP_CONTENT_ID));
		form.add(new Link<Void>("previous") {

			@Override
			public void onClick() {
				activeStepIndex--;
				form.replace(getActiveStep().render(STEP_CONTENT_ID));
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setEnabled(activeStepIndex > 0);
			}

			@Override
			protected void onComponentTag(ComponentTag tag) {
				super.onComponentTag(tag);
				
				if (activeStepIndex <= 0)
					tag.append("class", "disabled", " ");
			}
			
		});
		form.add(new Link<Void>("skip") {

			@Override
			public void onClick() {
				getActiveStep().getSkippable().skip();
				if (activeStepIndex == steps.size() - 1)
					finished(); 
				else
					form.replace(getActiveStep().render(STEP_CONTENT_ID));
			}
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getActiveStep().getSkippable() != null);
			}

		});
		form.add(new Button("next") {

			@Override
			public void onSubmit() {
				super.onSubmit();
				getActiveStep().complete();
				activeStepIndex++;
				form.replace(getActiveStep().render(STEP_CONTENT_ID));
			}
			
			@Override
			public void onError() {
				super.onError();
				form.error("Fix errors below");				
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(activeStepIndex < steps.size()-1);
			}

		});
		form.add(new Button("finish") {

			@Override
			public void onSubmit() {
				super.onSubmit();
				getActiveStep().complete();
				finished();
			}
			
			@Override
			public void onError() {
				super.onError();
				form.error("Fix errors below");
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(activeStepIndex == steps.size()-1);
			}

		});
		add(form);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CssResourceReference(Wizard.class, "wizard.css")));
	}

	private WizardStep getActiveStep() {
		return steps.get(activeStepIndex);
	}

	protected abstract void finished();

}
