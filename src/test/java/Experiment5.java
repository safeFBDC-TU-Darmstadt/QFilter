// Measures the time to generate secret shares of the lineitem table (only using the first four columns) for a variable
// number of user groups, as well as the combined file sizes for the tables (in Byte).
public class Experiment5 extends Experiment4 {

    @Override
    int[] getUserGroups() {
        return new int[]{2, 20, 40};
    }

}
