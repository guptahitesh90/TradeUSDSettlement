package com.jpmorgan.trade.service;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.jpmorgan.trade.constants.Currency;
import com.jpmorgan.trade.model.Instruction;
import com.jpmorgan.trade.repository.InstructionRepository;

/**
 * This class is responsible for generating incoming, outgoing & entity ranking reports.
 * @author Hitesh Gupta
 * 
 */
public class ReportService {

    private Map<LocalDate, Double> outgoingAmountPerDay = new HashMap<>();
    private Map<LocalDate, Double> incomingAmountPerDay = new HashMap<>();
    private Map<String, Double> outgoingEntityAmount = new HashMap<>();
    private Map<String, Double> incomingEntityAmount = new HashMap<>();
    
    private static final String BUY = "BUY";
    private static final String SELL = "SELL";
    private static DecimalFormat df = new DecimalFormat("0.00");

    /**
     * This method will process the instructions data, calculate the trade amount
     * & will store in maps based on Buy/Sell of a trade with date as key.
     */
    public void generateIncomingAndOutgoingReport() {

        InstructionRepository instructionRepository = new InstructionRepository();
        List<Instruction> instructions = instructionRepository.getInstructions();

        instructions.forEach((instruction) -> {
            updateSettlementDate(instruction);
        });
        instructions.forEach((instruction -> {
            Double usdAmount = instruction.getPricePerUnit() * instruction.getUnits() * instruction.getAgreedFx();
            if (SELL.equalsIgnoreCase(instruction.getType())) {
                if (incomingAmountPerDay.containsKey(instruction.getSettlementDate())) {
                    incomingAmountPerDay.put(instruction.getSettlementDate(), incomingAmountPerDay.get(instruction.getSettlementDate()) + usdAmount);
                } else {
                    incomingAmountPerDay.put(instruction.getSettlementDate(), usdAmount);
                }
                if (incomingEntityAmount.containsKey(instruction.getEntity())) {
                    incomingEntityAmount.put(instruction.getEntity(), incomingEntityAmount.get(instruction.getEntity()) + usdAmount);
                } else {
                    incomingEntityAmount.put(instruction.getEntity(), usdAmount);
                }
            } else if (BUY.equalsIgnoreCase(instruction.getType())) {
                if (outgoingAmountPerDay.containsKey(instruction.getSettlementDate())) {
                    outgoingAmountPerDay.put(instruction.getSettlementDate(), outgoingAmountPerDay.get(instruction.getSettlementDate()) + usdAmount);
                } else {
                    outgoingAmountPerDay.put(instruction.getSettlementDate(), usdAmount);
                }
                if (outgoingEntityAmount.containsKey(instruction.getEntity())) {
                    outgoingEntityAmount.put(instruction.getEntity(), outgoingEntityAmount.get(instruction.getEntity()) + usdAmount);
                } else {
                    outgoingEntityAmount.put(instruction.getEntity(), usdAmount);
                }
            }
        }));

    }

    /**
     * This method will display the trade buy amount in USD. 
     */
    public void displayOutGoingReport() {
        System.out.println("***** Outgoing/Buy Amount settled in USD ****** ");
        outgoingAmountPerDay.forEach((date, amount) -> {
            System.out.println("Date:" + date.format(DateTimeFormatter.ofPattern("dd MMM yyyy")) + " and amount $" + df.format(amount));
        });
        System.out.println("******************************************\n");
    }

    /**
     * This method will display the trade sell amount in USD. 
     */
    public void displayIncomingReport() {
    	
        System.out.println("***** Incoming/Sell Amount settled in USD ****** ");
        incomingAmountPerDay.forEach((date, amount) -> {
            System.out.println("Date:" + date.format(DateTimeFormatter.ofPattern("dd MMM yyyy")) + " and amount $" + df.format(amount));
        });
        System.out.println("******************************************\n");
    }

    /**
     * This method will generate the entity ranking report based on incoming & outgoing amount.
     */
    public void generateEntityRankingReport() {
        Map<String, Double> sortedOutgoingMap = new LinkedHashMap<>();
        outgoingEntityAmount.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .forEachOrdered(x -> sortedOutgoingMap.put(x.getKey(), x.getValue()));

        Map<String, Double> sortedIncomingMap = new LinkedHashMap<>();
        incomingEntityAmount.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .forEachOrdered(x -> sortedIncomingMap.put(x.getKey(), x.getValue()));

        System.out.println("***** Entity ranking based on highest outgoing/Buy Amount ****** ");
        
        sortedOutgoingMap.forEach((entity, amount) -> {
            System.out.println("Entity is ::" + entity + " and amount is :: $" + df.format(amount));
        });

        System.out.println("******************************************\n");

        System.out.println("***** Entity ranked based on highest Incoming/SELL Amount ****** ");

        sortedIncomingMap.forEach((entity, amount) -> {
            System.out.println("Entity is ::" + entity + " and amount is :: $" + df.format(amount));
        });

        System.out.println("******************************************\n");

    }

    /**
     * This method will set the actual settlement date for the trade
     * in instruction data object based on the day of week & currency.
     * @param instruction
     */
    public void updateSettlementDate(Instruction instruction) {

        LocalDate settlementDate = instruction.getSettlementDate();
        LocalDate actualSettlementDate = settlementDate;
        if (Currency.AED.equals(instruction.getCurrency()) || Currency.SAR.equals(instruction.getCurrency())) {
            switch (settlementDate.getDayOfWeek()) {
                case FRIDAY:
                    actualSettlementDate = settlementDate.plusDays(2);
                    break;
                case SATURDAY:
                    actualSettlementDate = settlementDate.plusDays(1);
                    break;
                default:
                    break;
            }
        } else {
            switch (settlementDate.getDayOfWeek()) {
                case SATURDAY:
                    actualSettlementDate = settlementDate.plusDays(2);
                    break;
                case SUNDAY:
                    actualSettlementDate = settlementDate.plusDays(1);
                    break;
                default:
                    break;
            }
        }
        instruction.setSettlementDate(actualSettlementDate);
    }
}