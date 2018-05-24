package org.xblink.rep.org.xmlpull.v1;

public class XmlPullParserException extends Exception {
	protected Throwable detail;
	protected int row = -1;
	protected int column = -1;

	public XmlPullParserException(String s) {
		super(s);
	}

	public XmlPullParserException(String msg, XmlPullParser parser, Throwable chain) {
		super((msg == null ? "" : msg + " ")
				+ (parser == null ? "" : "(position:" + parser.getPositionDescription() + ") ")
				+ (chain == null ? "" : "caused by: " + chain));

		if (parser != null) {
			this.row = parser.getLineNumber();
			this.column = parser.getColumnNumber();
		}
		this.detail = chain;
	}

	public Throwable getDetail() {
		return detail;
	}

	public int getLineNumber() {
		return row;
	}

	public int getColumnNumber() {
		return column;
	}

	public void printStackTrace() {
		if (detail == null) {
			super.printStackTrace();
		} else {
			synchronized (System.err) {
				System.err.println(super.getMessage() + "; nested exception is:");
				detail.printStackTrace();
			}
		}
	}

}
