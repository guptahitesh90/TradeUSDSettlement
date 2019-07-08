package com.jpmorgan.trade;

import com.jpmorgan.trade.service.ReportService;

/**
 * The Forex trade program implements an application that
 * displays the Trade Settlement incoming, outgoing amount
 * and entity rankings based on the amount.
 * 
 * @author Hitesh Gupta
 * @version 1.0
 * @since 2019-07-08
 */
public class ForexTradeMain {

	public static void main(String[] args) {
		ReportService reportService = new ReportService();

		reportService.generateIncomingAndOutgoingReport();
		reportService.displayOutGoingReport();
		reportService.displayIncomingReport();
		reportService.generateEntityRankingReport();

	}
}
