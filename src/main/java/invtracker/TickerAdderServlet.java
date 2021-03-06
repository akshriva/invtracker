package invtracker;

import invtracker.database.UpdateDatabase;
import invtracker.dto.TickerData;
import invtracker.impl.ProcessTicker;
import invtracker.impl.TickerReader;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.*;
import javax.naming.*;

import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TickerAdderServlet extends HttpServlet {
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// reading the user input
		PrintWriter out = response.getWriter();

		String ticker = request.getParameter("ticker");
		out.println("I got ... " + request.getParameter("windowSize") + "...");
		int windowSize = Integer.parseInt(request.getParameter("windowSize")
				.toString());
		out.println("I got ticker: " + ticker);

		try {
			// Create table if does not exist
			String tableName = ticker + windowSize;
			UpdateDatabase updateDB = new UpdateDatabase();
			if (!updateDB.tableExists(tableName)) {
				// create table for ticker
				updateDB.createTableForTickerSymbol(ticker, windowSize);

				// get data from URL and parse
				TickerReader tickerReader = new TickerReader();
				List<TickerData> tickerDataList = tickerReader.read(ticker,
						windowSize);
				if (tickerDataList != null && !tickerDataList.isEmpty()) {
					out.println(tickerDataList.toString());
				}
				out.println();
				out.println("\nAfter Processing");
				// get 100 day max & min for each date
				ProcessTicker processTicker = new ProcessTicker(tickerDataList,
						windowSize);
				List<TickerData> tickerDataListWithMarkers = processTicker
						.processFirstTime();
				if (tickerDataListWithMarkers != null
						&& !tickerDataListWithMarkers.isEmpty()) {
					out.println(tickerDataListWithMarkers.toString());
				}
				
				// save data in database
				updateDB.insertIntoTableForTickerSymbol(ticker, windowSize, tickerDataListWithMarkers);
				
				// update the page ...

			}
			else {
				out.println("oops the table already exists");
			}

			// re
			// String nextJSP = "/tickerGraphs.jsp";
			// RequestDispatcher dispatcher =
			// getServletContext().getRequestDispatcher(nextJSP);
			// dispatcher.forward(request,response);
			// List<Float> test = new ArrayList<Float>();
			// test.add(2.22F);
			// test.add(1.22F);
			// test.add(0.22F);
			// test.add(3.22F);
			// test.add(5.22F);
			// test.get(2);
			// out.println(Collections.max(test));
			//
			// // // adding ticker table
			// Connection result = null;
			// // try {
			// Context initialContext = new InitialContext();
			// DataSource datasource = (DataSource) initialContext
			// .lookup("java:jboss/datasources/MySQLDS");
			// result = datasource.getConnection();
			// Statement stmt = result.createStatement();
			//
			// String query = "select * from names;";
			// ResultSet rs = stmt.executeQuery(query);
			// while (rs.next()) {
			// out.println(rs.getString(1) + " " + rs.getString(2) + " "
			// + rs.getString(3) + "<br />");
			// }
		} catch (Exception ex) {
			out.println(ex.getStackTrace());
		}

		//
	}
}