package com.vdsp;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CmdRecMgr {
	private static ConcurrentLinkedQueue<CmdRec> cmdRecList=
		new ConcurrentLinkedQueue<CmdRec>();
	public static boolean addCmdRec(CmdRec rec){
		return cmdRecList.add(rec);
	}
	public static Iterator<CmdRec> getCmdRecIter(){
		return cmdRecList.iterator();
	}

	/*已发送命令记录*/
    public class CmdRec{
    	public int  	seqno;
    	public String 	cmd;
    	public Object 	object;
    	public CmdRec(String name,int sn,Object obj){
    		seqno=sn;
    		cmd=name;
    		object=obj;
    	}
    }
}
