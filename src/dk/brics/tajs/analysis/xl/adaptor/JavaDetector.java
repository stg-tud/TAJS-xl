package dk.brics.tajs.analysis.xl.adaptor;

import dk.brics.tajs.analysis.xl.adapter.LocalTAJSAdapter;
import dk.brics.tajs.analysis.js.NodeTransfer;
import dk.brics.tajs.flowgraph.Function;
import dk.brics.tajs.flowgraph.SourceLocation;
import dk.brics.tajs.flowgraph.jsnodes.*;
import dk.brics.tajs.lattice.*;
import dk.brics.tajs.options.Options;
import dk.brics.tajs.util.Pair;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class JavaDetector extends NodeTransfer {

    /**
     * Constructs a new JavaDetector object.
     */
    public JavaDetector() {}

    @Override
    public void visit(ReadVariableNode n) {

        if(n.getVariableName().equals("Java")) {
            SourceLocation sl = new SourceLocation.StaticLocationMaker(null).make(0, 0, 1, 1);
            JavaNode javaNode = new JavaNode(sl, Long.valueOf(1));
            javaNode.setIndex(1);
            ObjectLabel objectLabel = ObjectLabel.make(javaNode, ObjectLabel.Kind.JAVAOBJECT);
            Value javaObject = Value.makeObject(objectLabel);
            writeToRegisterAndAddMustReachDefs(n.getResultRegister(), javaObject, n);
        }
        else
            super.visit(n);
    }

    @Override
    public void visit(ReadPropertyNode n) {
        String propertyName = n.getPropertyString();
        int baseRegister = n.getBaseRegister();
        Value getBaseValue = c.getState().getRegisters().get(n.getBaseRegister());
        if(getBaseValue.isJavaObject() && n.getResultRegister()==-1){
            return;
        }else if(propertyName!=null && propertyName.equals("type") && c.getState().readRegister(baseRegister).isJavaObject())
            return;
        else {
            Value baseValue = c.getState().readRegister(baseRegister);
            if (baseValue.isJavaObject()) {
                if(n.getResultRegister()>0) {
                    Value fieldValue = LocalTAJSAdapter.getLocalTajsAdapter().readProperty(baseValue, propertyName);
                    writeToRegisterAndAddMustReachDefs(n.getResultRegister(), fieldValue, n);
                }
                return ;
            }
        }
        super.visit(n);
    }

    @Override
    public void visit(WritePropertyNode n){
        String propertyName = n.getPropertyString();
        int baseRegister = n.getBaseRegister();
        Value baseValue = c.getState().readRegister(baseRegister);
        int valueRegister = n.getValueRegister();
        Value assignedValue = c.getState().readRegister(valueRegister);
        if(baseValue.isJavaObject()){
            LocalTAJSAdapter.getLocalTajsAdapter().setProperty(baseValue, propertyName, assignedValue);
        }
        else
            super.visit(n);
    }

    @Override
    public void visit(CallNode n) {
       int baseRegister = n.getBaseRegister();
        String functionName = n.getPropertyString();

        if(n.isConstructorCall()){
            int functionRegister = n.getFunctionRegister();
            Value baseValue = c.getState().readRegister(functionRegister);
            if(baseValue.isJSJavaTYPE()) {
                String javaType = baseValue.getObjectLabels().stream().map(ol -> ol.getJavaName()).findFirst().get();
                Value v = LocalTAJSAdapter.getLocalTajsAdapter().newObject(n.getIndex(), javaType);
                writeToRegisterAndAddMustReachDefs(n.getResultRegister(), v, n);
                State newState = c.getState().clone();
                c.propagateToBasicBlock(newState, n.getBlock().getSingleSuccessor(), newState.getContext());
                return;
            }
        } else if (baseRegister > 0) {
            Value baseValue = c.getState().readRegister(baseRegister);
        if(n.getNumberOfArgs()>0) {
            int argumentRegister = n.getArgRegister(0);
                if(baseValue.isJavaObject()){
                if (functionName.equals("type")) {
                    String javaType = c.getState().readRegister(argumentRegister).getStr();
                    ObjectLabel.Kind jol = ObjectLabel.Kind.JS_JAVATYPE;
                    ObjectLabel ol =  ObjectLabel.make(n, jol);
                    ol.setJavaName(javaType);
                    Value v = Value.makeObject(ol).setDontDelete().setDontEnum().setReadOnly();
                    writeToRegisterAndAddMustReachDefs(n.getResultRegister(), v, n);
                    State newState = c.getState().clone();
                    c.propagateToBasicBlock(newState, n.getBlock().getSingleSuccessor(), newState.getContext());
                    return;
                } else {
                    Value argument = c.getState().readRegister(n.getArgRegister(0));
                    List<Value> params = new LinkedList<>();
                    params.add(argument);
                    Value resultValue = LocalTAJSAdapter.getLocalTajsAdapter().callFunction(baseValue,n.getPropertyString(), params);
                    System.out.print(resultValue);
                    if(resultValue!=Value.makeAbsent()){
                        writeToRegisterAndAddMustReachDefs(n.getResultRegister(), resultValue, n);
                        State newState = c.getState().clone();
                        c.propagateToBasicBlock(newState, n.getBlock().getSingleSuccessor(), newState.getContext());
                    }
                    return;
                }
                }
        }
            if (baseValue.isJavaObject()) {
                if(n.getResultRegister()>0){
                    //String javaFullClassName = baseValue.getJavaName();
                    //if(baseValue.getObjectLabels().stream().findFirst().get().getNode().getIndex()>0){
                    //    try {
                            //Value v = LocalTAJSAdapter.getLocalTajsAdapter().readObject(baseValue); //TODO
                            writeToRegisterAndAddMustReachDefs(n.getResultRegister(), baseValue, n);
                    }
                   // else
                    //TODO    writeToRegisterAndAddMustReachDefs(n.getResultRegister(), Value.makeUndef(), n);

                    State newState = c.getState().clone();
                    c.propagateToBasicBlock(newState, n.getBlock().getSingleSuccessor(), newState.getContext());
                    return;
                }
            }

        super.visit(n);
    }

    public void visit(JavaNode n){}

    public void visit(WriteVariableNode n){
        if(this.c.getState().getRegisters().get(n.getValueRegister())!=null && c.getState().readRegister(n.getValueRegister()).isJavaObject()){
            Value value = c.getState().readRegister(n.getValueRegister());
            Value v = value;
            if (Options.get().isBlendedAnalysisEnabled()) {
                v = c.getAnalysis().getBlendedAnalysis().getVariableValue(value, n, c.getState());
                if (v.isNone()) {
                    c.getState().setToBottom();
                    return;
                }
            }
            Pair<Set<ObjectLabel>,Boolean> objsDef = pv.writeVariable(n.getVariableName(), v, true);
            Function f = n.getBlock().getFunction();
            if (f.getParameterNames().contains(n.getVariableName())) { // TODO: review
                ObjectLabel arguments_obj = ObjectLabel.make(f.getEntry().getFirstNode(), ObjectLabel.Kind.ARGUMENTS);
                pv.writeProperty(arguments_obj, PKey.StringPKey.make(Integer.toString(f.getParameterNames().indexOf(n.getVariableName()))), v);
            }
            m.visitPropertyWrite(n, objsDef.getFirst(), Value.makeTemporaryStr(n.getVariableName()));
            m.visitVariableOrProperty(n, n.getVariableName(), n.getSourceLocation(), v, c.getState().getContext(), c.getState());
            if (objsDef.getSecond())
                c.getState().getMustEquals().addMustEquals(n.getValueRegister(), MustEquals.getSingleton(objsDef.getFirst()), PKey.StringPKey.make(n.getVariableName()));
        } else
            super.visit(n);
    }

    private void writeToRegisterAndAddMustReachDefs(int register, Value v, Node n){
        c.getState().writeRegister(register, v);
        c.getState().getMustReachingDefs().addReachingDef(register, n);
    }
}

