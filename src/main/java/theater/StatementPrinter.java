package theater;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

/**
 * This class generates a statement for a given invoice of performances.
 */
public class StatementPrinter {
    private Invoice invoice;
    private Map<String, Play> plays;

    public StatementPrinter(Invoice invoice, Map<String, Play> plays) {
        this.setInvoice(invoice);
        this.setPlays(plays);
    }

    /**
     * Returns a formatted statement of the invoice associated with this printer.
     * @return the formatted statement
     * @throws RuntimeException if one of the play types is not known
     */
    public String statement() {
        int totalAmount = 0;
        int volumeCredits = 0;
        final StringBuilder result =
                new StringBuilder("Statement for " + getInvoice().getCustomer() + System.lineSeparator());

        final NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);

        for (Performance per : getInvoice().getPerformances()) {

            // add volume credits
            volumeCredits += Math.max(per.getAudience() - Constants.BASE_VOLUME_CREDIT_THRESHOLD, 0);
            // add extra credit for every five comedy attendees
            if ("comedy".equals(getPlay(per).getType())) {
                volumeCredits += per.getAudience() / Constants.COMEDY_EXTRA_VOLUME_FACTOR;
            }

            // print line for this order
            result.append(String.format("  %s: %s (%s seats)%n", getPlay(per).getName(), format.format(
                    getAmount(per) / Constants.PERCENT_FACTOR), per.getAudience()));
            totalAmount += getAmount(per);
        }
        result.append(String.format("Amount owed is %s%n", format.format(totalAmount / Constants.PERCENT_FACTOR)));
        result.append(String.format("You earned %s credits%n", volumeCredits));
        return result.toString();
    }

    private Play getPlay(Performance per) {
        return getPlays().get(per.getPlayID());
    }

    private int getAmount(Performance per) {
        int thisAmount;
        switch (getPlay(per).getType()) {
            case "tragedy":
                thisAmount = Constants.TRAGEDY_BASE_AMOUNT;
                if (per.getAudience() > Constants.TRAGEDY_AUDIENCE_THRESHOLD) {
                    thisAmount += Constants.TRAGEDY_OVER_BASE_CAPACITY_PER_PERSON * (
                            per.getAudience() - Constants.TRAGEDY_AUDIENCE_THRESHOLD);
                }
                break;
            case "comedy":
                thisAmount = Constants.COMEDY_BASE_AMOUNT;
                if (per.getAudience() > Constants.COMEDY_AUDIENCE_THRESHOLD) {
                    thisAmount += Constants.COMEDY_OVER_BASE_CAPACITY_AMOUNT
                            + (Constants.COMEDY_OVER_BASE_CAPACITY_PER_PERSON
                            * (per.getAudience() - Constants.COMEDY_AUDIENCE_THRESHOLD));
                }
                thisAmount += Constants.COMEDY_AMOUNT_PER_AUDIENCE * per.getAudience();
                break;
            default:
                throw new RuntimeException(String.format("unknown type: %s", getPlay(per).getType()));
        }
        return thisAmount;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public Map<String, Play> getPlays() {
        return plays;
    }

    public void setPlays(Map<String, Play> plays) {
        this.plays = plays;
    }
}
