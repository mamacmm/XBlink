package org.xblink.core.doc.impl;

import java.io.StringWriter;
import java.io.Writer;

import org.xblink.core.doc.AbstractDocWriter;
import org.xblink.rep.org.xmlpull.mxp1_serializer.MXSerializer;
import org.xblink.rep.org.xmlpull.v1.XmlSerializer;

/**
 * XML格式Writer，基于XPP3的MXSerializer实现。
 * 
 * @author 胖五(pangwu86@gmail.com)
 */
public class XPP3Writer extends AbstractDocWriter {

	private XmlSerializer innerWriter;

	public XPP3Writer(Writer writer) {
		super(writer);
		innerWriter = new MXSerializer();
		try {
			innerWriter.setOutput(writer);
		}
		catch (Exception e) {
			throw new RuntimeException("XPP3Writer Unable To Initialize", e);
		}
	}

	private void writeIndent() throws Exception {
		for (int idx = 0; idx < indentIndex; idx++) {
			innerWriter.text(indentString);
		}
	}

	private void writeBR() throws Exception {
		innerWriter.text(br);
	}

	public void writeStartDocument() throws Exception {
		// TODO 还写不写来？
		innerWriter.startDocument(encoding, null);
	}

	public void writeEndDocument() throws Exception {
		innerWriter.endDocument();
	}

	public void writeStartTag(String tagName) throws Exception {
		writeBR();
		writeIndent();
		indentIndex++;
		innerWriter.startTag(null, tagName);
	}

	public void writeEndTag(String tagName) throws Exception {
		writeBR();
		indentIndex--;
		writeIndent();
		innerWriter.endTag(null, tagName);
	}

	public void writeAttribute(String name, String value) throws Exception {
		if (null != value) {
			innerWriter.attribute(null, name, value);
		}
	}

	public void writeText(String text) throws Exception {
		innerWriter.text(text);
	}

	public void writeElementText(String tagName, String text) throws Exception {
		boolean isNull = (null == text);
		writeBR();
		writeIndent();
		if (isNull) {
			innerWriter.startTag(null, tagName);
			innerWriter.endTag(null, tagName);
		} else {
			innerWriter.startTag(null, tagName);
			innerWriter.text(text);
			innerWriter.endTag(null, tagName);
		}
	}

	public void writeReference(String tagName, String refName, String text) throws Exception {
		innerWriter.attribute(null, refName, text);
		innerWriter.endTag(null, tagName);
		indentIndex--;
	}

	public void close() throws Exception {}

	public void flush() throws Exception {
		innerWriter.flush();
	}

	public String getString() {
		if (writer instanceof StringWriter) {
			return writer.toString();
		}
		return null;
	}

}
