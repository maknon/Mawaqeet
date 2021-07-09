package com.maknoon.mawaqeet;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.atan;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

public class CalcUtils
{
	static double Frac(double Num)
	{
		int iPart;
		double fPart;

		iPart = (int) Num;
		fPart = Num - iPart;
		//System.out.println("Integer part = " + iPart);
		//System.out.println("Fractional part = " + fPart);
		return fPart;
	}

	static double myArcCos(double Num)
	{
		if ((abs(Num) > 1))
			return Num;

		double result;
		if ((Num == -1))
			result = 4 * myArcTan(1, (byte) 0);
		else
			if ((Num == 0))
				result = 2 * myArcTan(1, (byte) 0);
			else
				if (Num == 1)
					result = 0;
				else
					result = myArcTan(-Num / sqrt(-Num * Num + 1), (byte) 0) + (2 * myArcTan(1, (byte) 0));
		return result;
	}

	static double myArcSin(double Num)
	{
		double A = 0;

		if (abs(Num) != 1)
			A = myArcTan(Num / sqrt(abs(1 - P(Num, 2))), (byte) 0);

		return A;
	}

	static double P(double X, int I)
	{
		int Y = abs(I);

		double Result = 1.0;
		while (Y > 0)
		{
			while (!Odd(Y))
			{
				Y = Y >>> 1;
				X = X * X;
			}

			//Y = Dec(Y);
			Y -= 1;
			Result = Result * X;
		}

		if (I < 0)
			Result = 1.0 / Result;
		return Result;
	}

	static boolean Odd(int Num)
	{
		return Num % 2 != 0;
	}

	static double myArcTan(double Num, byte Quarter)
	{
		double A = atan(Num) * (180 / PI);

		if (Quarter != 0)
		{
			if (myMod(A, 90) != 0)
				A = myMod(A, 90) + 90 * (Quarter - 1);
			else
				A = myMod(A, 90) + 90 * (Quarter);
		}

		return A;
	}

	static double myTan(double Num)
	{
		if (myCos(Num) != 0)
			return mySin(Num) / myCos(Num);
		else
			return 0;
	}

	static double myCos(double Num)
	{
		return cos(Num * (PI / 180));
	}

	static byte GetQuarter(double Lng)
	{
		Lng = myMod(Lng, 360);
		if (Lng <= 90)
			return 1;
		else
			if (Lng <= 180)
				return 2;
			else
				if (Lng <= 270)
					return 3;
				else
					return 4;
	}

	static double mySin(double Num)
	{
		return sin(Num * (PI / 180));
	}

	static double myMod(double Num, double Divisor)
	{
		double R = Num / Divisor;
		if (R < 0)
			R = R - 1;

		if (Divisor != 0)
			return Num - Divisor * (int) R;
		else
			return Num;
	}

	// Only for US !!!!
	static double getAltitude(Double longitude, Double latitude)
	{
		double result = Double.NaN;
		final String url = "https://nationalmap.gov/epqs/"
				+ "pqs.php?x=" + longitude
				+ "&y=" + latitude
				+ "&units=Feet&output=xml";

		// Require API key and an account for google maps and 25000 hits limit per day
		String url_google = "https://maps.googleapis.com/maps/api/elevation/"
				+ "xml?locations=" + latitude
				+ "," + longitude
				+ "&sensor=true";

		try
		{
			final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			final Document doc = docBuilder.parse(url);

			// normalize text representation
			doc.getDocumentElement().normalize();

			// The results are contained in a single <double> node
			final NodeList listOfDoubles = doc.getElementsByTagName("Elevation");

			if (listOfDoubles.getLength() > 0)
			{
				Node elevationNode = listOfDoubles.item(0);
				Element elevationElement = (Element) elevationNode;
				return Double.parseDouble(elevationElement.getFirstChild().getNodeValue());
			}
		}
		catch (IOException | SAXException | ParserConfigurationException e)
		{
			e.printStackTrace();
		}

		/* Working as well
		HttpClient httpClient = HttpClientBuilder.create().build();
		HttpContext localContext = new BasicHttpContext();
		HttpGet httpGet = new HttpGet(url);
		try
		{
			final HttpResponse response = httpClient.execute(httpGet, localContext);
			Log.e(TAG, "response " + response);
			final HttpEntity entity = response.getEntity();
			if (entity != null)
			{
				final InputStream instream = entity.getContent();
				int r = -1;
				final StringBuilder respStr = new StringBuilder();
				while ((r = instream.read()) != -1)
					respStr.append((char) r);
				String tagOpen = "<Elevation>";
				String tagClose = "</Elevation>";
				if (respStr.indexOf(tagOpen) != -1)
				{
					int start = respStr.indexOf(tagOpen) + tagOpen.length();
					int end = respStr.indexOf(tagClose);
					String value = respStr.substring(start, end);
					result = Double.parseDouble(value);
				}
				instream.close();
			}
		}
		catch (ClientProtocolException | IOException e)
		{
			e.printStackTrace();
		}
		*/
		return result;
	}
}