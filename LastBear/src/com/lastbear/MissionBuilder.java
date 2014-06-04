package com.lastbear;

import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class MissionBuilder extends DefaultHandler {

	private String type = null;
	private ArrayList<Coordinate> coordinates = new ArrayList<Coordinate>();
	private ArrayList<Mission> missions = null;
	private Mission mission = null;
	private InputStream is;

	public MissionBuilder(InputStream is) {
		super();
		this.is = is;
	}

	public ArrayList<Mission> getMissions() {
		if (missions == null) {
			missions = new ArrayList<Mission>();
			try {
				SAXParserFactory factory = SAXParserFactory.newInstance();
				XMLReader reader = factory.newSAXParser().getXMLReader();
				reader.setContentHandler(this);
				reader.parse(new InputSource(is));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return missions;
	}

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
		// System.out.println("start Document ~");
	}

	@Override
	public void endDocument() throws SAXException {
		super.endDocument();
		// System.out.println("end Document ~");
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		// System.out.println("start Element ~");
		if (localName.equals("group")) {
			// String groupId = attributes.getValue("id");
		} else if (localName.equals("mission")) {
			String t;
			String missionName = "";
			t = attributes.getValue("name");
			if (t != null) {
				missionName = t;
			}
			float missionUnit = 1.0f;
			t = attributes.getValue("unit");
			if (t != null) {
				try {
					missionUnit = Float.parseFloat(t);
				} catch (NumberFormatException e) {
				}
			}
			float missionRotate = 0.0f;
			t = attributes.getValue("rotate");
			if (t != null) {
				try {
					missionRotate = Float.parseFloat(t);
				} catch (NumberFormatException e) {
				}
			}
			float missionXscale = 0.0f;
			t = attributes.getValue("xscale");
			if (t != null) {
				try {
					missionXscale = Float.parseFloat(t);
				} catch (NumberFormatException e) {
				}
			}
			float missionYscale = 0.0f;
			t = attributes.getValue("yscale");
			if (t != null) {
				try {
					missionYscale = Float.parseFloat(t);
				} catch (NumberFormatException e) {
				}
			}
			mission = new Mission(missionName, missionUnit, missionRotate,
					missionXscale, missionYscale);
		} else if (localName.equals("shape")) {
			if (mission != null) {
				coordinates.clear();
			}
		} else if (localName.equals("piece")) {
			if (mission != null) {
				coordinates.clear();
				type = attributes.getValue("type");
			}
		} else if (localName.equals("coordinate")) {
			if (mission != null) {
				String x = attributes.getValue("x");
				String y = attributes.getValue("y");
				Coordinate c = new Coordinate(Float.parseFloat(x),
						Float.parseFloat(y));
				coordinates.add(c);
			}
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		super.endElement(uri, localName, qName);
		// System.out.println("end Element ~");
		if (localName.equals("mission")) {
			missions.add(mission);
			mission = null;
		} else if (localName.equals("shape")) {
			if (mission != null) {
				mission.setShape(coordinates);
				coordinates.clear();
			}
		} else if (localName.equals("piece")) {
			if (mission != null) {
				mission.addPiece(type, coordinates);
				coordinates.clear();
			}
		}
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		super.characters(ch, start, length);
		// System.out.println("characters ~");
	}
}
