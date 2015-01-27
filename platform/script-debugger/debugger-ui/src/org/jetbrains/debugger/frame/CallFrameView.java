package org.jetbrains.debugger.frame;

import com.intellij.ui.ColoredTextContainer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.intellij.xdebugger.frame.XCompositeNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.concurrency.Promise;
import org.jetbrains.debugger.*;

import java.util.List;

public final class CallFrameView extends StackFrameImplBase implements VariableContext {
  private final DebuggerViewSupport viewSupport;
  private final CallFrame callFrame;

  private final Script script;

  private final boolean inLibraryContent;

  public CallFrameView(@NotNull CallFrame callFrame, @NotNull DebuggerViewSupport viewSupport, @Nullable Script script) {
    this(callFrame, viewSupport.getSourceInfo(script, callFrame), viewSupport, script);
  }

  public CallFrameView(@NotNull CallFrame callFrame,
                       @Nullable SourceInfo sourceInfo,
                       @NotNull DebuggerViewSupport viewSupport,
                       @Nullable Script script) {
    super(sourceInfo);

    this.viewSupport = viewSupport;
    this.callFrame = callFrame;
    this.script = script;

    // isInLibraryContent call could be costly, so we compute it only once (our customizePresentation called on each repaint)
    inLibraryContent = sourceInfo != null && viewSupport.isInLibraryContent(sourceInfo, script);
  }

  @Nullable
  public Script getScript() {
    return script;
  }

  @Override
  protected boolean isInFileScope() {
    List<Scope> scopes = callFrame.getVariableScopes();
    return scopes.size() == 1 && scopes.get(0).isGlobal();
  }

  @Override
  protected XDebuggerEvaluator createEvaluator() {
    return viewSupport.createFrameEvaluator(this);
  }

  @Override
  public Object getEqualityObject() {
    return callFrame.getEqualityObject();
  }

  @Override
  protected boolean isInLibraryContent() {
    return inLibraryContent;
  }

  @Override
  protected void customizeInvalidFramePresentation(ColoredTextContainer component) {
    assert sourceInfo == null;

    String scriptName = script == null ? "unknown" : script.getUrl().trimParameters().toDecodedForm();
    int line = callFrame.getLine();
    component.append(line != -1 ? scriptName + ':' + line : scriptName, SimpleTextAttributes.ERROR_ATTRIBUTES);
  }

  @Override
  public void computeChildren(@NotNull XCompositeNode node) {
    node.setAlreadySorted(true);
    ScopeVariablesGroup.createAndAddScopeList(node, callFrame.getVariableScopes(), this, callFrame);
  }

  @NotNull
  public CallFrame getCallFrame() {
    return callFrame;
  }

  @NotNull
  @Override
  public EvaluateContext getEvaluateContext() {
    return callFrame.getEvaluateContext();
  }

  @Nullable
  @Override
  public String getName() {
    return null;
  }

  @Nullable
  @Override
  public VariableContext getParent() {
    return null;
  }

  @Override
  public boolean watchableAsEvaluationExpression() {
    return true;
  }

  @NotNull
  @Override
  public DebuggerViewSupport getViewSupport() {
    return viewSupport;
  }

  @NotNull
  @Override
  public Promise<MemberFilter> getMemberFilter() {
    return viewSupport.getMemberFilter(this);
  }

  @NotNull
  public Promise<MemberFilter> getMemberFilter(@NotNull Scope scope) {
    return ScopeVariablesGroup.createVariableContext(scope, this, callFrame).getMemberFilter();
  }

  @Nullable
  @Override
  public Scope getScope() {
    return null;
  }
}