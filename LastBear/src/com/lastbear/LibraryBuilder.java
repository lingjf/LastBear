package com.lastbear;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;

public class LibraryBuilder extends DefaultHandler {

	private static LibraryBuilder libraryBuilder = null;

	public static LibraryBuilder getInstance() {
		return libraryBuilder;
	}

	public static LibraryBuilder getInstance(Context context) {
		if (libraryBuilder == null) {
			libraryBuilder = new LibraryBuilder(context);
		}
		return libraryBuilder;
	}

	private Context context = null;
	private LibraryGroup libraryGroup;

	private ArrayList<LibraryGroup> libraryGroups = new ArrayList<LibraryGroup>();

	private ArrayList<Coordinate> coordinates = new ArrayList<Coordinate>();
	private ArrayList<Polygon> polygons = new ArrayList<Polygon>();

	private LibraryBuilder(Context context) {
		super();
		this.context = context;
		try {
			InputStream is = context.getAssets().open("lastbear_library.xml");
			SAXParserFactory factory = SAXParserFactory.newInstance();
			XMLReader reader = factory.newSAXParser().getXMLReader();
			reader.setContentHandler(this);
			reader.parse(new InputSource(is));
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
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
			libraryGroup = new LibraryGroup(context, attributes.getValue("id"),
					attributes.getValue("name"));
		} else if (localName.equals("thumbnail")) {
			polygons.clear();
		} else if (localName.equals("piece")) {
			coordinates.clear();
		} else if (localName.equals("coordinate")) {
			String x = attributes.getValue("x");
			String y = attributes.getValue("y");
			Coordinate c = new Coordinate(Float.parseFloat(x),
					Float.parseFloat(y));
			coordinates.add(c);
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		super.endElement(uri, localName, qName);
		// System.out.println("end Element ~");

		if (localName.equals("group")) {
			libraryGroups.add(libraryGroup);
			libraryGroup = null;
		} else if (localName.equals("thumbnail")) {
			libraryGroup.setThumbnail(polygons);
			polygons.clear();
		} else if (localName.equals("piece")) {
			polygons.add(new Polygon(coordinates));
			coordinates.clear();
		}
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		super.characters(ch, start, length);
		// System.out.println("characters ~");
	}

	public ArrayList<LibraryGroup> getGroups() {
		return libraryGroups;
	}

	public LibraryGroup getGroup(String groupId) {
		for (int i = 0; i < libraryGroups.size(); i++) {
			if (libraryGroups.get(i).getId().equals(groupId))
				return libraryGroups.get(i);
		}
		return null;
	}

	public LibraryGroup getPrevGroup(String groupId) {
		int j = -1;
		for (int i = 0; i < libraryGroups.size(); i++) {
			if (libraryGroups.get(i).getId().equals(groupId))
				j = i;
		}
		if (j == -1)
			return null;
		if (j - 1 < 0)
			return null;
		return libraryGroups.get(j - 1);
	}

	public LibraryGroup getNextGroup(String groupId) {
		int j = -1;
		for (int i = 0; i < libraryGroups.size(); i++) {
			if (libraryGroups.get(i).getId().equals(groupId))
				j = i;
		}
		if (j == -1)
			return null;
		if (j + 1 >= libraryGroups.size())
			return null;
		return libraryGroups.get(j + 1);
	}
}
