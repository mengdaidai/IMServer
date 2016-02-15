package Server;


public class Protocol {
	public static final int LENGTH_BYTE= 4;
	public static final int TYPE_BYTE = 1;
	public static final int MESSAGE_ID_BYTE = 1;
	public static final int FROM_BYTE = 8;
	public static final int TOKEN_BYTE = 32;
	public static final int TO_BYTE = 8;
	public static final int TIME_BYTE = 8;
	public static final int LENGTH_START = 0;
	public static final int TYPE_START = LENGTH_START+LENGTH_BYTE;
	public static final int MESSAGE_ID_START = TYPE_START+TYPE_BYTE;
	public static final int FROM_START = MESSAGE_ID_START+MESSAGE_ID_BYTE;
	public static final int TOKEN_START = FROM_START+FROM_BYTE;
	public static final int TO_START = TOKEN_START+TOKEN_BYTE;
	public static final int TIME_START = TO_START+TO_BYTE;
	public static final int HEAD_LENGTH = TIME_START+TIME_BYTE;
	public static final byte REGISTER_TYPE = 1;
	public static final byte REGISTER_RESPONSE_TYPE = 2;
	public static final byte LOGIN_TYPE = 3;
	public static final byte LOGIN_RESPONSE_TYPE = 4;
	public static final byte HEARTBEAT_TYPE = 5;
	public static final byte HEARTBEAT_RESPONSE_TYPE = 6;
	public static final byte TEXT_TYPE = 7;
	public static final byte PICTURE_TYPE = 8;
	public static final byte VOICE_TYPE = 9;
	public static final byte FRIEND_TYPE = 10;
	public static final byte TRANSMITION_RESPONSE_TYPE = 11;
	public static final byte FILE_TYPE = 12;
	public static final byte FILE_TRANS_TYPE = 13;
	public static final byte FILE_RESPONSE_TYPE = 14;
	
	
}
