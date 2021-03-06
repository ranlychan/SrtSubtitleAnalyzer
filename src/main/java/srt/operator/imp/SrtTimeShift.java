package srt.operator.imp;

import cn.hutool.core.lang.Console;
import srt.core.SrtNode;
import srt.core.SrtTime;
import srt.exception.NullOnOperateSrtNodesListenerException;
import srt.listener.OnOperateSrtNodesListener;
import srt.operator.SrtOperator;

import javax.management.JMException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import static java.lang.System.exit;
import static srt.operator.imp.SrtNodeTimeOperate.*;


public class SrtTimeShift extends SrtOperator {

    private String shiftType;
    private SrtTime shiftSrtTime;
    private int shiftMsecond;
    private SrtNode startNode;

    public SrtTimeShift(HashMap<String, Object> parameters) {
        super(parameters);
        if (parameters.get("shiftType") != null) {
            this.shiftType = (String) parameters.get("shiftType");
        }

        if (parameters.get("srtMsecond") != null) {
            this.shiftMsecond = (int) parameters.get("srtMsecond");
        }

        if (parameters.get("startNode") != null) {
            this.startNode = (SrtNode) parameters.get("startNode");
        }


    }

    @Override
    public Object execute(List<SrtNode> srtNodes, OnOperateSrtNodesListener onOperateSrtNodesListener) {
        if(onOperateSrtNodesListener == null){
            onOperateSrtNodesListener.onOperationFail(new NullOnOperateSrtNodesListenerException());
        }

        if(startNode != null){
            Console.log("startNode != null");
            doTimeShift(srtNodes,startNode,onOperateSrtNodesListener);
        }else{
            doTimeShift(srtNodes,onOperateSrtNodesListener);
        }
        return null;
    }

    public void doTimeShift(List<SrtNode> srtNodes, OnOperateSrtNodesListener onOperateSrtNodesListener){
        onOperateSrtNodesListener.onOperationStart();
        Iterator it = srtNodes.iterator();
        while(it.hasNext()){
            SrtNode node = (SrtNode)it.next();
            switch (shiftType){
                case SHIFT_TYPE_PLUS:
                    if(plusBoth(node,shiftMsecond)){
                        break;
                    }else{
                        //????????????????????????????????????srtNodes
                        if(!minusBoth(node,shiftMsecond)){
                            onOperateSrtNodesListener.onOperationFail(new Exception("??????????????????????????????????"));
                            exit(-1);
                        }
                        onOperateSrtNodesListener.onOperationFail(new Exception("??????????????"));
                    }
                    break;
                case SHIFT_TYPE_MINUS:
                    if(minusBoth(node,shiftMsecond)){
                        continue;
                    }else{
                        //????????????????????????????????????srtNodes
                        if(!plusBoth(node,shiftMsecond)){
                            onOperateSrtNodesListener.onOperationFail(new Exception("??????????????????????????????????"));
                            exit(-1);
                        }
                        onOperateSrtNodesListener.onOperationFail(new Exception("??????????????"));
                    }
                    break;
            }
        }
        onOperateSrtNodesListener.onOperationSuccess(srtNodes);
    }

    public void doTimeShift(List<SrtNode> srtNodes,SrtNode startNode, OnOperateSrtNodesListener onOperateSrtNodesListener){

        ListIterator it = srtNodes.listIterator();
        boolean isFoundFirst = false;
        SrtNode node = null;

        while(!isFoundFirst) {
            node = (SrtNode)it.next();
            //Console.log("??????????????sid="+node.getSid());
            if (node.getSid() == startNode.getSid()) {
                //Console.log("????????????????sid="+node.getSid());
                isFoundFirst = true;
                break;
            } else {
                continue;
            }
        }

        //????????????????
        switch (shiftType){
            case SHIFT_TYPE_PLUS:
                it.previous();
                while(it.hasNext()){
                    node = (SrtNode)it.next();
                    if(plusBoth(node,shiftMsecond)){
                        continue;
                    }else{
                        //????????????????????????????????????srtNodes
                        if(!minusBoth(node,shiftMsecond)){
                            onOperateSrtNodesListener.onOperationFail(new Exception("??????????????????????????????????"));
                            exit(-1);
                        }
                        onOperateSrtNodesListener.onOperationFail(new Exception("??????????????"));
                        break;
                    }
                }
                break;
            case SHIFT_TYPE_MINUS:
                //it.next();
                while (it.hasPrevious()){
                    node = (SrtNode)it.previous();
                    if(minusBoth(node,shiftMsecond)){
                        continue;
                    }else {
                        //????????????????????????????????????????????????srtNodes
                        if(!plusBoth(node,shiftMsecond)){
                            onOperateSrtNodesListener.onOperationFail(new Exception("??????????????????????????????????"));
                            exit(-1);
                        }
                        onOperateSrtNodesListener.onOperationFail(new Exception("??????????????"));
                    }
                }

                break;
        }


        if(isFoundFirst){
            onOperateSrtNodesListener.onOperationSuccess(srtNodes);
        }else {
            onOperateSrtNodesListener.onOperationFail(new Exception("??????????????????????????????????"));
        }

    }
}
