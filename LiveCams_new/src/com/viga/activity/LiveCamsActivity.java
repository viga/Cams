package com.viga.activity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.vdsp.CmdId;
import com.vdsp.CmdRecMgr;
import com.vdsp.CmdSend;
import com.vdsp.DispatchHandler;
import com.vdsp.MessageParam;
import com.vdsp.NoticeId;
import com.vdsp.Vca;
import com.vdsp.CmdRecMgr.CmdRec;
import com.viga.engine.DataProc;
import com.viga.engine.SettingAndStatus;
import com.viga.utils.Utils;
import com.viga.utils.Utils.SDFileWriter;
import com.viga.utils.Utils.ThreadExt;

public class LiveCamsActivity extends Activity {
	private final static String TAG = "LIVECAMS";
	private GridView mainGridView = null;
	private SimpleAdapter gridViewAdapter = null;
	private ArrayList<HashMap<String, Object>> gridViewList = null;
	private Utils utils = new Utils();
	private ThreadExt vcaMsgProc = null;
	private Vca vca = new Vca();
	private Vca.Param vcaParam = vca.new Param();
	private Handler oldHandler = null;
	private AudioManager mAudioManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.main);
		mainGridView = (GridView) findViewById(R.id.mainGridView);
		oldHandler = DispatchHandler.setHandler(handler);
		mAudioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

		SettingAndStatus.init(this);
		SettingAndStatus.load(null);
		setupMainGridView();

		if (SettingAndStatus.settings.autologin) {
			openAndStartVca(null);
		}
	}

	private void openBlueTooth() {
		mAudioManager.setBluetoothScoOn(true);
		mAudioManager.setMode(AudioManager.MODE_IN_CALL);
		mAudioManager.setStreamMute(AudioManager.STREAM_VOICE_CALL, true);
		mAudioManager.startBluetoothSco();
	}

	@Override
	public void onStop() {
		if (DispatchHandler.isCurrent(handler)) {
			DispatchHandler.setHandler(oldHandler);
		}
		super.onStop();
	}

	@Override
	public void onDestroy() {
		stopAndCloseVca();
		SettingAndStatus.exit();
		super.onDestroy();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		/* 系统登录Activity回调 */
		if (1 == requestCode && 1 == resultCode) {
			String operation = intent.getStringExtra("operation");
			if ("login".equals(operation)) {
				String ipaddr = intent.getStringExtra("srvipaddr");
				String port = intent.getStringExtra("srvport");
				String devid = intent.getStringExtra("devid");
				SettingAndStatus.settings.srvipaddr = Utils.ipaddrToInt(ipaddr);
				SettingAndStatus.settings.srvport = Short.parseShort(port);
				SettingAndStatus.settings.devid = Integer.parseInt(devid);
				openAndStartVca(null);
			}
			/* 系统登出Activity回调 */
		} else if (2 == requestCode && 2 == resultCode) {
			String operation = intent.getStringExtra("operation");
			if ("logout".equals(operation)) {
				stopAndCloseVca();
			}
			/* 参数设置Activity回调 */
		} else if (3 == requestCode && 3 == resultCode) {
			String operation = intent.getStringExtra("operation");
			if ("modify".equals(operation)) {

			}
			/* 手机录像Activity回调 */
		} else if (4 == requestCode && 4 == resultCode) {
			// 停止音视频处理线程
			DataProc.stopRecAndTx();

			// 发送停止上传命令
			if (SettingAndStatus.phoneDataSending) {
				SettingAndStatus.phoneDataSending = false;
				CmdSend.stopSendData(false);
				Vca.Arg arg = vca.new Arg(-1, -1);
				Vca.control(Vca.OPID_CLRDST, arg);
			}

			// 停止数据接收播放服务
			if (SettingAndStatus.dataReceiving) {
				SettingAndStatus.dataReceiving = false;
				CmdSend.stopPlayRtv(false);
				Vca.Arg arg = vca.new Arg(-1, -1);
				Vca.control(Vca.OPID_CLRSRC, arg);
				DataProc.stopRxAndPlay();
			}
			/* 视频播放Activity回调 */
		} else if (5 == requestCode && 5 == resultCode) {
			SettingAndStatus.dataReceiving = false;
			SettingAndStatus.deviceDataSending = false;
			SettingAndStatus.isplaymode = false;
			if (SettingAndStatus.settings.audioduplex1) {
				DataProc.stopAudioRecAndTx();
				CmdSend.stopSendData(false);
				Vca.Arg arg = vca.new Arg(-1, -1);
				Vca.control(Vca.OPID_CLRDST, arg);
			} else if (SettingAndStatus.deviceDataSending) {
				CmdSend.stopSendData(false);
			}
			DataProc.stopRxAndPlay();
			CmdSend.stopPlayRtv(false);
			Vca.Arg arg = vca.new Arg(-1, -1);
			Vca.control(Vca.OPID_CLRSRC, arg);
		}
	}

	/* 初始化主界面 */
	private void setupMainGridView() {
		HashMap<String, Object> map;

		gridViewList = new ArrayList<HashMap<String, Object>>();
		map = new HashMap<String, Object>();
		map.put("ItemImage", R.drawable.setting);
		map.put("ItemText", getString(R.string.setting));
		gridViewList.add(map);
		map = new HashMap<String, Object>();
		map.put("ItemImage", R.drawable.login);
		map.put("ItemText", getString(R.string.login));
		gridViewList.add(map);
		map = new HashMap<String, Object>();
		map.put("ItemImage", R.drawable.snappic);
		map.put("ItemText", getString(R.string.snappic));
		gridViewList.add(map);
		map = new HashMap<String, Object>();
		map.put("ItemImage", R.drawable.record);
		map.put("ItemText", getString(R.string.record));
		gridViewList.add(map);
		map = new HashMap<String, Object>();
		map.put("ItemImage", R.drawable.play);
		map.put("ItemText", getString(R.string.play));
		gridViewList.add(map);
		map = new HashMap<String, Object>();
		map.put("ItemImage", R.drawable.upldpic);
		map.put("ItemText", getString(R.string.upldpic));
		gridViewList.add(map);
		map = new HashMap<String, Object>();
		map.put("ItemImage", R.drawable.browser);
		map.put("ItemText", getString(R.string.browser));
		gridViewList.add(map);
		map = new HashMap<String, Object>();
		map.put("ItemImage", R.drawable.devctrl);
		map.put("ItemText", getString(R.string.devctrl));
		gridViewList.add(map);

		gridViewAdapter = new SimpleAdapter(this, gridViewList, R.layout.main_item, new String[] {
				"ItemImage", "ItemText" }, new int[] { R.id.ItemImage, R.id.ItemText });
		mainGridView.setAdapter(gridViewAdapter);
		mainGridView.setOnItemClickListener(new GvItemClickListener());
	}

	/* 修改GridView中Login项中的文字 */
	private void modifyLoginItem(String status) {
		HashMap<String, Object> map = gridViewList.get(1);
		map.clear();
		map.put("ItemImage", R.drawable.login);
		map.put("ItemText", status);
		gridViewAdapter.notifyDataSetChanged();
	}

	/* 通知UI主线程修改GridView中Login项中的文字 */
	public void submitLoginStatus(String status) {
		android.os.Message msg = new android.os.Message();
		msg.what = 253;
		msg.obj = status;
		handler.sendMessage(msg);
	}

	/* 主界面消息处理 */
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case DispatchHandler.SHOW_SHORTNOTICE:// 弹出本地短通知
			case DispatchHandler.SHOW_LONGNOTICE: // 弹出本地长通知
				String info = (String) msg.obj;
				Context context = getApplicationContext();
				Toast toast = Toast
						.makeText(context, info,
								DispatchHandler.SHOW_SHORTNOTICE == msg.what ? Toast.LENGTH_SHORT
										: Toast.LENGTH_LONG);
				toast.show();
				break;
			case 253:/* 通知UI主线程修改GridView中Login项中的文字 */
				String status = (String) msg.obj;
				modifyLoginItem(status);
				break;
			case 252:/* 弹出让用户选择播放实时视频对话框 */
				int rtvcnt = msg.arg1;
				int mode = msg.arg2;
				int[] rtvlist = (int[]) msg.obj;
				new SelectRtvDialog(LiveCamsActivity.this, rtvlist, rtvcnt, mode).show();
				break;
			default:
				super.handleMessage(msg);
				break;
			}
		}
	};

	/* 与服务器建立连接回调 */
	private void connectedNoticeProcess(MessageParam msgparam) {
		SettingAndStatus.vcaStatus.status = SettingAndStatus.VcaStatus.CONNECTED;
		Log.v(TAG, "成功与服务器建立链接!");
		submitLoginStatus(getString(R.string.logining));
	}

	/* 登陆服务器回调 */
	private void loginNoticeProcess(MessageParam msgparam) {
		if (msgparam.flag[msgparam.RESULT]) {
			if (1 == msgparam.result || 400 == msgparam.result) {
				SettingAndStatus.vcaStatus.status = SettingAndStatus.VcaStatus.LOGINED;
				SettingAndStatus.loginTime = new Date();
				String srvtype = !msgparam.flag[msgparam.SRVTYPE] ? "服务器" : (0 == msgparam.srvtype ? "服务器"
						: (1 == msgparam.srvtype ? "背负设备" : "未知类型服务器"));
				Log.v(TAG, "成功登陆" + srvtype + "!");
				submitLoginStatus(getString(R.string.logout));
			} else {
				Log.v(TAG, "登陆服务器失败!");
				DispatchHandler.submitShortNotice("警告：登陆服务器失败!");
			}
		} else {
			Log.v(TAG, "无效的登陆通知!");
		}
	}

	/* 与服务器断开回调 */
	private void disconnectedNoticeProcess(MessageParam msgparam) {
		SettingAndStatus.vcaStatus.status = SettingAndStatus.VcaStatus.STARTED;
		SettingAndStatus.insertLogRecord(SettingAndStatus.loginTime, new Date());
		Log.v(TAG, "与服务器的链接已断开!");
		DispatchHandler.submitLongNotice("警告：与服务器的链接已断开!");
		submitLoginStatus(getString(R.string.connecting));
	}

	/* 应答包丢失回调 */
	private void ackdropedNoticeProcess(MessageParam msgparam) {
		if (msgparam.flag[msgparam.ERRNO] && msgparam.flag[msgparam.SEQNO]) {
			Iterator<CmdRec> it = CmdRecMgr.getCmdRecIter();
			while (it.hasNext()) {
				CmdRec rec = it.next();
				if (rec.seqno == msgparam.seqno) {
					if ("request_uploadsnap".equals(rec.cmd)) {
						Log.v(TAG, "上传抓拍照片" + (String) rec.object + ")失败!");
						DispatchHandler.submitLongNotice("警告：上传抓拍照片(" + (String) rec.object + ")失败!");
					} else if ("request_getrtvlist".equals(rec.cmd)) {
						Log.v(TAG, "请求实时视频列表失败!");
						DispatchHandler.submitLongNotice("警告：请求实时视频列表失败!");
						if (!SettingAndStatus.isplaymode) {
							if (SettingAndStatus.isrecordmode) {
								startRecordActivity();
							}
						} else {
							SettingAndStatus.isplaymode = false;
						}
						/* 在此添加其它命令应答丢失处理实现 */
					}
				}
			}
		} else {
			Log.v(TAG, "无效的应答包丢失通知!");
		}
	}

	/* 心跳超时回调 */
	private void hbtimeoutNoticeProcess(MessageParam msgparam) {
		if (msgparam.flag[msgparam.TIMECNT]) {
			Log.v(TAG, "心跳回复超时(" + msgparam.timecnt + ")!");
			DispatchHandler.submitLongNotice("警告：心跳回复超时!\n(心跳频率：" + vcaParam.hbperiod + "秒;超时门限："
					+ vcaParam.hbtimeout + "秒;超时时间：" + msgparam.timecnt + "秒)");
		} else {
			Log.v(TAG, "无效的心跳回复超时通知!");
		}
	}

	/* 应答包处理回调 */
	private void ackAndRecordProcess(MessageParam msgparam, Vca.Message message) {
		Iterator<CmdRec> it = CmdRecMgr.getCmdRecIter();
		while (it.hasNext()) {
			CmdRec rec = it.next();
			if (msgparam.seqno == rec.seqno) {
				if (rec.cmd.equals(msgparam.cmd)) {
					if ("request_uploadsnap".equals(rec.cmd)) {
						if (1 == msgparam.result) {
							Log.v(TAG, "成功上传照片" + (String) rec.object + "!");
							DispatchHandler.submitShortNotice("提示：成功上传照片(" + rec.object.toString().trim()+ ")!");
						} else {
							Log.v(TAG, "上传照片(" + (String) rec.object + ")失败!");
							DispatchHandler.submitLongNotice("警告：上传照片(" + (String) rec.object + ")失败!");
						}
						it.remove();
					} else if ("request_senddata".equals(rec.cmd)) {
						if (1 == msgparam.result) {
							if (msgparam.flag[msgparam.PORT]) {
								Vca.Arg arg = vca.new Arg(1, -1);
								arg.ip[0] = SettingAndStatus.settings.srvipaddr;
								arg.port[0] = msgparam.port;
								Vca.control(Vca.OPID_ADDDST, arg);
								SettingAndStatus.phoneDataSending = true;
							} else {
								SettingAndStatus.deviceDataSending = true;
							}
							Log.v(TAG,
									"成功上传" + (String) rec.object + "("
											+ Utils.intToIpaddr(SettingAndStatus.settings.srvipaddr) + ":"
											+ msgparam.port + ")!");
							DispatchHandler.submitShortNotice("提示：成功上传" + (String) rec.object + "!");
						} else {
							Log.v(TAG, "上传" + (String) rec.object + "失败!");
							DispatchHandler.submitShortNotice("警告：上传" + (String) rec.object + "失败!");
						}
						it.remove();
					} else if ("request_getrtvlist".equals(rec.cmd)) {
						boolean isok = false;
						if (1 == msgparam.result) {
							if (msgparam.flag[msgparam.RTVLIST]) {
								Log.v(TAG, "实时播放列表包括：");
								int rtvcnt = 0;
								int[] rtvlist = new int[msgparam.RTVMAXCNT];
								for (int i = 0; i < msgparam.RTVMAXCNT; i++) {
									switch (msgparam.rtvlist[i]) {
									case 0:
										Log.v(TAG, "\t服务器下发视频;");
										rtvlist[rtvcnt++] = 0;
										break;
									case 1:
										Log.v(TAG, "\t背负设备前置视频;");
										rtvlist[rtvcnt++] = 1;
										break;
									case 2:
										Log.v(TAG, "\t背负设备后置视频;");
										rtvlist[rtvcnt++] = 2;
										break;
									default:
										if (-1 != msgparam.rtvlist[i]) {
											Log.v(TAG, "\t无有效视频(" + msgparam.rtvlist[i] + ")!");
										}
										break;
									}
								}
								if (rtvcnt > 0) {
									/* 弹出让用户选择播放实时视频对话框 */
									android.os.Message msg = new android.os.Message();
									msg.what = 252;
									msg.arg1 = rtvcnt;
									msg.arg2 = SettingAndStatus.isplaymode ? 0
											: (SettingAndStatus.isrecordmode ? 1 : -1);
									msg.obj = rtvlist;
									handler.sendMessage(msg);
									isok = true;
								} else {
									Log.v(TAG, "实时播放列表中无有效视频!");
									DispatchHandler.submitShortNotice("警告：实时播放列表中无有效视频!");
								}
							} else {
								Log.v(TAG, "实时播放列表为空!");
								DispatchHandler.submitShortNotice("警告：实时播放列表为空!");
							}
						} else {
							Log.v(TAG, "获取实时播放列表失败!");
							DispatchHandler.submitShortNotice("警告：获取实时播放列表失败!");
						}
						it.remove();

						// 直接启动手机录像功能
						if (!isok && SettingAndStatus.isrecordmode) {
							startRecordActivity();
						}
					} else if ("request_playrtv".equals(rec.cmd)) {
						if (1 == msgparam.result) {
							if (msgparam.flag[msgparam.PORT]) {
								SettingAndStatus.dataReceiving = true;

								// 启动音视频接收播放线程
								/* startRxAndPlay(); */
								DataProc.startRxAndPlay(0, 8, null, null);

								// 添加数据源地址
								Vca.Arg arg = vca.new Arg(1, -1);
								arg.ip[0] = SettingAndStatus.settings.srvipaddr;
								arg.port[0] = msgparam.port;
								Vca.control(Vca.OPID_ADDSRC, arg);

								/* 检测是否需要上传语音 */
								if (SettingAndStatus.settings.audioduplex1) {
									new Thread(new Runnable() {
										public void run() {
											// 发送请求上传数据命令
											CmdSend.requestSendData(0, true, false, true);
											// 准备录音文件
											SDFileWriter arecorder = null;
											if (SettingAndStatus.settings.avrecord) {
												SimpleDateFormat format = new SimpleDateFormat(
														"yyyyMMddHHmmssSSS");
												String filename = format.format(new Date()) + ".spx";
												arecorder = utils.new SDFileWriter();
												if (!arecorder.open("LiveCams/video", filename)) {
													arecorder.close();
													arecorder = null;
													Log.v(TAG, "无法录音，请检查SD卡!");
													DispatchHandler.submitLongNotice("警告：无法录像，请检查SD卡!");
												} else {
													String fullname = arecorder.getFileFullName();
													Log.v(TAG, "开始录音(" + fullname + "!");
													DispatchHandler.submitShortNotice("提示：开始录音(" + fullname
															+ ")!");
												}
											}

											// 启动音频采集处理任务
											DataProc.startAudioRecAndTx(8, arecorder);
										}
									}).start();
								}

								Log.v(TAG,
										"成功播放" + (String) rec.object + "("
												+ Utils.intToIpaddr(SettingAndStatus.settings.srvipaddr)
												+ ":" + msgparam.port + ")!");
								DispatchHandler.submitShortNotice("提示：成功播放" + (String) rec.object + "!");
							} else {
								Log.v(TAG, "无效应答消息!");
								DispatchHandler.submitShortNotice("警告：播放" + (String) rec.object + "失败!");
							}
						} else {
							Log.v(TAG, "播放" + (String) rec.object + "失败!");
							DispatchHandler.submitShortNotice("警告：播放" + (String) rec.object + "失败!");
						}
						it.remove();
					} else if ("notice_uploadsnap".equals(rec.cmd)) {
						if (1 == msgparam.result) {
							if (msgparam.flag[msgparam.PICCNT] && msgparam.piccnt > 0
									&& msgparam.flag[msgparam.PICTYPE] && msgparam.flag[msgparam.PICLEN]) {
								for (int i = 0, offset = 0; i < msgparam.PICMAXCNT; i++) {
									if (msgparam.piclen[i] > 0) {
										if (0 == msgparam.pictype[i] || 1 == msgparam.pictype[i]) {
											Utils.SDFileWriter pic = utils.new SDFileWriter();
											String filename = (String) rec.object;
											if (msgparam.flag[msgparam.DATETIME]) {
												SimpleDateFormat format = new SimpleDateFormat(
														"yyyy-MM-dd HH:mm:ss SSS");
												try {
													Date date = format.parse(msgparam.datetime);
													format = new SimpleDateFormat("yyyyMMddHHmmssSSS");
													filename = format.format(date);
												} catch (ParseException e) {
												}
											}
											if (pic.open("LiveCams/photo", filename + "-" + i + ".jpg")) {
												pic.write(message.data, offset, msgparam.piclen[i]);
												pic.close();
											}
										}
										offset += msgparam.piclen[i];
									}
								}
							} else {
								Log.v(TAG, "无效应答消息!");
								DispatchHandler.submitShortNotice("警告：通知背负上传抓拍图片失败!");
							}
						} else {
							Log.v(TAG, "通知背负上传抓拍图片失败!");
							DispatchHandler.submitShortNotice("警告：通知背负上传抓拍图片失败!");
						}
						it.remove();
					} else if ("get_videoquality".equals(rec.cmd)) {
						if (1 == msgparam.result) {
							if (msgparam.flag[msgparam.BRIGHTNESS]) {
								SettingAndStatus.videoQuality.brightness = msgparam.brightness;
							}
							if (msgparam.flag[msgparam.HUE]) {
								SettingAndStatus.videoQuality.hue = msgparam.hue;
							}
							if (msgparam.flag[msgparam.CONTRAST]) {
								SettingAndStatus.videoQuality.contrast = msgparam.contrast;
							}
							if (msgparam.flag[msgparam.SATURATION]) {
								SettingAndStatus.videoQuality.saturation = msgparam.saturation;
							}
						} else {
							Log.v(TAG, "获取视频质量参数失败!");
							DispatchHandler.submitShortNotice("警告：获取视频质量参数失败!");
						}
						it.remove();
					} else if ("get_encoderparam".equals(rec.cmd)) {
						if (1 == msgparam.result) {
							if (msgparam.flag[msgparam.VIDEOSIZE]) {
								SettingAndStatus.encoderParam.videosize = msgparam.videosize;
							}
							if (msgparam.flag[msgparam.BITRATE]) {
								SettingAndStatus.encoderParam.bitrate = msgparam.bitrate;
							}
							if (msgparam.flag[msgparam.FRAMERATE]) {
								SettingAndStatus.encoderParam.framerate = msgparam.framerate;
							}
							if (msgparam.flag[msgparam.MAXFRAMERATE]) {
								SettingAndStatus.encoderParam.maxframerate = msgparam.maxframerate;
							}
							if (msgparam.flag[msgparam.INTERVAL]) {
								SettingAndStatus.encoderParam.interval = msgparam.interval;
							}
						} else {
							Log.v(TAG, "获取视频质量参数失败!");
							DispatchHandler.submitShortNotice("警告：获取视频质量参数失败!");
						}
						it.remove();
						/* 在此添加其它命令应答处理实现 */
					} else {
					}
				} else {
					Log.v(TAG, "命令名称不匹配:" + rec.cmd + "/" + msgparam.cmd + "\n");
					DispatchHandler.submitLongNotice("警告：序号为"
							+ msgparam.seqno
							+ "的命令("
							+ msgparam.cmd
							+ ")处理异常("
							+ (-1 == msgparam.errno ? "应答超时" : (-2 == msgparam.errno ? "应答包过大"
									: (-3 == msgparam.errno ? "消息堵塞" : (-4 == msgparam.errno ? "内存不足"
											: "未知原因")))) + ")!");
					it.remove();
				}
			}
		}
	}

	/* VCA消息处理线程 */
	private void msgProc(ThreadExt thread) {
		Log.v(TAG, "VCA消息处理线程已运行!");
		Vca.Message msg = vca.new Message(vcaParam.msglen, 1024 * 1024);
		MessageParam msgparam = new MessageParam();
		while (!thread.isExit()) {
			if (Vca.getMessage(msg, 1000)) {
				String param = (new String(msg.param)).trim();
				msgparam.reset();
				if (NoticeId.NOTICE == msg.type) {
					Log.v(TAG, "收到一个通知消息:\n" + param);
					if (msgparam.parserNotice(param.getBytes())) {
						switch (msgparam.id) {
						case NoticeId.CONNECTED:
							connectedNoticeProcess(msgparam);
							break;
						case NoticeId.LOGIN:
							loginNoticeProcess(msgparam);
							break;
						case NoticeId.DISCONNECTED:
							disconnectedNoticeProcess(msgparam);
							break;
						case NoticeId.ACKDROPED:
							ackdropedNoticeProcess(msgparam);
							break;
						case NoticeId.HBTIMEOUT:
							hbtimeoutNoticeProcess(msgparam);
							break;
						default:
							Log.v(TAG, "未知通知消息!");
							break;
						}
					} else {
						Log.v(TAG, "无效通知消息!");
					}
				} else if (CmdId.CMDACK == msg.type) {
					if (msgparam.parserCmdAck(param.getBytes())) {
						Log.v(TAG, "收到一个" + (CmdId.ACK == msgparam.id ? "应答" : "命令") + "消息:\n" + param);
						switch (msgparam.id) {
						case CmdId.ACK:
							if (msgparam.flag[msgparam.SEQNO] && msgparam.flag[msgparam.CMD]
									&& msgparam.flag[msgparam.RESULT]) {
								ackAndRecordProcess(msgparam, msg);
							} else {
								Log.v(TAG, "无效应答消息!");
							}
							break;
						/* 在此添加其它命令处理实现 */
						default:
							Log.v(TAG, "未知命令消息!");
							break;
						}
					}
				} else {
					Log.v(TAG, "收到一个未知类型消息(" + msg.type + ")!");
				}
			}
		}
		Log.v(TAG, "VCA消息处理线程已结束!");
	}

	/* 打开和启动VCA模块 */
	private boolean openAndStartVca(View v) {
		boolean ret = false;
		vcaParam.srvipaddr = SettingAndStatus.settings.srvipaddr;
		vcaParam.srvport = SettingAndStatus.settings.srvport;
		vcaParam.speexen = SettingAndStatus.settings.speexen;
		vcaParam.encmode = SettingAndStatus.settings.encmode;
		vcaParam.decmode = SettingAndStatus.settings.decmode;
		vcaParam.isplrt = SettingAndStatus.settings.isplrt;
		vcaParam.osplrt = SettingAndStatus.settings.osplrt;
		vcaParam.h264decen = SettingAndStatus.settings.h264decen;
		vcaParam.dispheight = SettingAndStatus.settings.fullscreen ? SettingAndStatus.displaywidth : 0;
		vcaParam.dispwidth = SettingAndStatus.settings.fullscreen ? SettingAndStatus.displayheight : 0;
		if (Vca.open(vcaParam)) {
			Log.v(TAG, "成功打开通信模块!");
			SettingAndStatus.vcaStatus.status = SettingAndStatus.VcaStatus.OPENED;
			if (Vca.start(40, SettingAndStatus.settings.sndport, 1024 * 1024,
					SettingAndStatus.settings.rcvport, 1024 * 1024, SettingAndStatus.settings.timeout)) {
				SettingAndStatus.vcaStatus.status = SettingAndStatus.VcaStatus.STARTED;
				modifyLoginItem(getString(R.string.connecting));
				Log.v(TAG, "成功启动通信模块!");

				vcaMsgProc = utils.new ThreadExt(new Runnable() {
					public void run() {
						msgProc(vcaMsgProc);
					}
				});
				vcaMsgProc.start();
				SettingAndStatus.msgProcStatus = true;
				ret = true;
			} else {
				SettingAndStatus.vcaStatus.status = SettingAndStatus.VcaStatus.INIT;
				Vca.close();
				Log.v(TAG, "启动通信模块失败!");
				Utils.showLongNotice(this, "警告：启动通信模块失败!");
			}
		} else {
			Log.v(TAG, "打开通信模块失败!");
			Utils.showLongNotice(this, "警告：打开通信模块失败!");
		}
		return ret;
	}

	/* 停止和删除VCA模块 */
	private void stopAndCloseVca() {
		if (SettingAndStatus.vcaStatus.status >= SettingAndStatus.VcaStatus.STARTED) {
			if (SettingAndStatus.msgProcStatus) {
				vcaMsgProc.finish();
			}
			SettingAndStatus.vcaStatus.status = SettingAndStatus.VcaStatus.STOPPING;
			modifyLoginItem(getString(R.string.stopping));
			new Thread(new Runnable() {
				public void run() {
					Log.v(TAG, "通信模块停止!");
					Vca.stop();// 耗时较长
					Log.v(TAG, "通信模块关闭!");
					Vca.close();
					SettingAndStatus.vcaStatus.status = SettingAndStatus.VcaStatus.INIT;
					submitLoginStatus(getString(R.string.login));
				}
			}).start();
		} else if (SettingAndStatus.vcaStatus.status >= SettingAndStatus.VcaStatus.OPENED) {
			Vca.close();
			Log.v(TAG, "通信模块关闭!");
			SettingAndStatus.vcaStatus.status = SettingAndStatus.VcaStatus.INIT;
			modifyLoginItem(getString(R.string.login));
		}
	}

	/**/
	private void startPlayActivity(int id) {
		Intent intent = new Intent(LiveCamsActivity.this, VideoPlayActivity.class);
		intent.putExtra("requestcode", 5);
		intent.putExtra("videoid", id);
		startActivityForResult(intent, 5);
	}

	/**/
	private void startRecordActivity() {
		// 发送请求上传数据命令
		CmdSend.requestSendData(0, true, true, true);

		// 启动视频采集处理线程
		DataProc.startVideoRecAndTx(0, null);

		// 启动录像界面
		Intent intent = new Intent(this, VideoRecordActivity.class);
		intent.putExtra("requestcode", 4);
		startActivityForResult(intent, 4);
	}

	/* 参数设置点击处理函数 */
	private void settingClickProcess() {
		Intent intent = new Intent(this, SettingActivity.class);
		intent.putExtra("requestcode", 3);
		startActivityForResult(intent, 3);
	}

	/* 系统登录点击处理函数 */
	private void loginClickProcess(View v) {
		Intent intent = new Intent(this, LoginActivity.class);
		if (SettingAndStatus.VcaStatus.INIT == SettingAndStatus.vcaStatus.status) {
			if (SettingAndStatus.settings.logincheck) {
				intent.putExtra("requestcode", 1);// login check
				startActivityForResult(intent, 1);
			} else {
				openAndStartVca(v);
			}
		} else {
			if (SettingAndStatus.vcaStatus.status >= SettingAndStatus.VcaStatus.STOPPING) {
				Utils.showShortNotice(this, "提示：正在停止通信模块，请稍候操作!");
			} else if (SettingAndStatus.vcaStatus.status >= SettingAndStatus.VcaStatus.LOGINED) {
				if (SettingAndStatus.settings.logoutcheck) {
					AlertDialog dialog = new AlertDialog.Builder(LiveCamsActivity.this)
							.setMessage(getString(R.string.logoutcheck)).setCancelable(false)
							.setPositiveButton("确 认", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									SettingAndStatus.insertLogRecord(SettingAndStatus.loginTime, new Date());
									stopAndCloseVca();
								}
							}).setNegativeButton("取 消", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int id) {
									dialog.cancel();
								}
							}).create();
					dialog.show();
				} else {
					SettingAndStatus.insertLogRecord(SettingAndStatus.loginTime, new Date());
					stopAndCloseVca();
				}
			} else if (SettingAndStatus.vcaStatus.status >= SettingAndStatus.VcaStatus.STARTED
					&& SettingAndStatus.msgProcStatus) {
				if (SettingAndStatus.settings.logincheck) {
					intent.putExtra("requestcode", 2);// stop login
					startActivityForResult(intent, 2);
				} else {
					stopAndCloseVca();
				}
			} else {
				Utils.showShortNotice(this, "提示：操作过于频繁，请稍后操作!");
			}
		}
	}

	/* 手机拍照点击处理函数 */
	private void snapPicClickProcess() {
		Intent intent = new Intent(this, SnapPicActivity.class);
		startActivity(intent);
	}

	/* 手机录像点击处理函数 */
	private void avRecordClickProcess() {
		// 检查是否启动双向语音？
		SettingAndStatus.isrecordmode = true;
		if (SettingAndStatus.settings.useBlueTooth) {
			openBlueTooth();
			// Log.e("bt",""+SettingAndStatus.settings.useBlueTooth);
		}
		if (SettingAndStatus.settings.audioduplex0) {
			// 发送请求实时播放列表命令
			if (CmdSend.requestGetRtvList(true)) {
				return;
			}
		}

		// 启动手机录像功能
		startRecordActivity();
	}

	/* 在线播放点击处理函数 */
	private void avPlayClickProcess() {
		// 发送请求实时播放列表命令
		openBlueTooth();
		SettingAndStatus.isplaymode = true;
		if (!CmdSend.requestGetRtvList(true)) {
			SettingAndStatus.isplaymode = false;
		}
	}

	public void nativeScanClickprocess() {
		// 浏览LiveCams文件夹中的文件
		Intent intent = new Intent(LiveCamsActivity.this, NationScanActivity.class);
		startActivity(intent);
	}

	/* 主界面点击操作处理函数 */
	private class GvItemClickListener implements OnItemClickListener {
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			switch (arg2) {
			case 0:// 参数设置
				Log.v(TAG, "点击了【" + getString(R.string.setting).toString() + "】项！");
				settingClickProcess();
				break;
			case 1:// 系统登陆
				HashMap<String, Object> map = gridViewList.get(1);
				String itemtext = (String) map.get("ItemText");
				Log.v(TAG, "点击了【" + itemtext + "】项！");
				loginClickProcess(arg1);
				break;
			case 2:// 手机拍照
				Log.v(TAG, "点击了【" + getString(R.string.snappic).toString() + "】项！");
				snapPicClickProcess();
				break;
			case 3:// 手机录像
				Log.v(TAG, "点击了【" + getString(R.string.record).toString() + "】项！");
				avRecordClickProcess();
				break;
			case 4:// 在线播放
				Log.v(TAG, "点击了【" + getString(R.string.play).toString() + "】项！");
				avPlayClickProcess();
				break;
			case 5:// 本地上传
				Log.v(TAG, "点击了【" + getString(R.string.upldpic).toString() + "】项！");
				break;
			case 6:// 本地浏览
				Log.v(TAG, "点击了【" + getString(R.string.browser).toString() + "】项！");
				nativeScanClickprocess();
				break;
			case 7:// 设备控制
				Log.v(TAG, "点击了【" + getString(R.string.devctrl).toString() + "】项！");
				break;
			default:
				break;
			}
		}
	}

	/* 选择要播放的实时视频对话框 */
	private class SelectRtvDialog extends Dialog {
		private int rtvlist[];
		private int count;
		private int mode;

		public SelectRtvDialog(Context context, int[] list, int cnt, int mode) {
			super(context);
			SelectRtvDialog.this.mode = mode;
			SelectRtvDialog.this.count = cnt;
			if (cnt > 0) {
				SelectRtvDialog.this.rtvlist = new int[cnt];
				for (int i = 0; i < cnt; i++) {
					SelectRtvDialog.this.rtvlist[i] = list[i];
				}
			}
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			if (0 == SelectRtvDialog.this.mode) {
				setContentView(R.layout.select_rtv);
				setTitle("请选择要播放的实时音视频：");
				Button[] bt = new Button[3];
				bt[0] = (Button) findViewById(R.id.serverVideoBt);
				bt[1] = (Button) findViewById(R.id.frontVideoBt);
				bt[2] = (Button) findViewById(R.id.behindVideoBt);
				bt[0].setEnabled(false);
				bt[1].setEnabled(false);
				bt[2].setEnabled(false);
				for (int i = 0; i < SelectRtvDialog.this.count; i++) {
					int id = SelectRtvDialog.this.rtvlist[i];
					switch (id) {
					case 0:
						bt[0].setEnabled(true);
						bt[0].setOnClickListener(new View.OnClickListener() {
							public void onClick(View v) {
								String name = getString(R.string.servervideo);
								CmdSend.requestPlayRtv(0, name, true);
								SelectRtvDialog.this.dismiss();
								startPlayActivity(0);
							}
						});
						break;
					case 1:
						bt[1].setEnabled(true);
						bt[1].setOnClickListener(new View.OnClickListener() {
							public void onClick(View v) {
								String name = getString(R.string.frontvideo);
								CmdSend.requestPlayRtv(1, name, true);
								SelectRtvDialog.this.dismiss();
								startPlayActivity(1);
							}
						});
						break;
					case 2:
						bt[2].setEnabled(true);
						bt[2].setOnClickListener(new View.OnClickListener() {
							public void onClick(View v) {
								String name = getString(R.string.behindvideo);
								CmdSend.requestPlayRtv(2, name, true);
								SelectRtvDialog.this.dismiss();
								startPlayActivity(2);
							}
						});
						break;
					default:
						break;
					}
				}
			} else if (1 == SelectRtvDialog.this.mode) {
				setContentView(R.layout.select_rta);
				setTitle("请选择对讲对象：");
				Button[] bt = new Button[2];
				bt[0] = (Button) findViewById(R.id.serverAudioBt);
				bt[1] = (Button) findViewById(R.id.backpackAudioBt);
				bt[0].setEnabled(false);
				bt[1].setEnabled(false);
				for (int i = 0; i < SelectRtvDialog.this.count; i++) {
					int id = SelectRtvDialog.this.rtvlist[i];
					switch (id) {
					case 0:
						bt[0].setEnabled(true);
						bt[0].setOnClickListener(new View.OnClickListener() {
							public void onClick(View v) {
								String name = getString(R.string.serveraudio);
								CmdSend.requestPlayRtv(0, name, true);
								SelectRtvDialog.this.dismiss();
								startRecordActivity();
							}
						});
						break;
					case 1:
					case 2:
						bt[1].setEnabled(true);
						bt[1].setOnClickListener(new View.OnClickListener() {
							public void onClick(View v) {
								String name = getString(R.string.frontvideo);
								CmdSend.requestPlayRtv(1, name, true);
								SelectRtvDialog.this.dismiss();
								startRecordActivity();
							}
						});
						break;
					default:
						break;
					}
				}
			}
			((Button) findViewById(R.id.cancelBt)).setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					SettingAndStatus.isplaymode = false;
					SelectRtvDialog.this.cancel();
					if (1 == SelectRtvDialog.this.mode) {
						startRecordActivity();
					}
				}
			});

			WindowManager winmgr = getWindowManager();
			Display disp = winmgr.getDefaultDisplay();
			LayoutParams param = getWindow().getAttributes();
			param.width = (int) (disp.getWidth() * 0.9);
			param.height = (int) (disp.getHeight() * 0.6);
			getWindow().setAttributes(param);
		}
	}
}