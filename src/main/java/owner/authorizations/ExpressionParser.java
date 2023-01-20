package owner.authorizations;

import owner.exceptions.ExpressionParsingException;

import java.util.ArrayList;
import java.util.List;

public class ExpressionParser {
    public static List<BooleanExpression> parseExpression(String bool_expr) throws ExpressionParsingException {
        List<BooleanExpression> boolEx;
        bool_expr = bool_expr.toLowerCase();
        String normalization = bool_expr.replace("and","").replace("or","").replace("(","").replace(")","");
        String[] split = normalization.split("\\s+");
        if(bool_expr.toLowerCase().contains("and") && bool_expr.toLowerCase().contains("or")){
            if(split.length>3){
                throw new ExpressionParsingException("The boolean expression contains more than 3 different conditional statements");
            }
            boolEx = validateComplexExpression(bool_expr.toLowerCase());
        }else{
            if(bool_expr.isBlank() || bool_expr.isEmpty()){
                throw new ExpressionParsingException("The boolean expression is empty");
            }
            if(split.length>4){
                throw new ExpressionParsingException("The boolean expression contains more than 4 similar conditional statements");
            }
            boolEx = validateSimpleExpression(bool_expr.toLowerCase());
        }
        return boolEx;
    }
    public static void main(String[] args) throws ExpressionParsingException {
        List<BooleanExpression> test = parseExpression("(1 OR 2) AND 3 OR 4");
    }

    private static List<BooleanExpression> validateComplexExpression(String bool_expr) throws ExpressionParsingException {
        List<BooleanExpression> boolEx = new ArrayList<>();
        String s = bool_expr.replace("(", "").replace(")","");
        String[] split = s.split("\\s+");
        if(split[0].matches(".*\\d.*") && split[1].matches("and|or") && split[2].matches(".*\\d.*")
                && split[3].matches("and|or") && split[4].matches(".*\\d.*")){
            int idxAnd = bool_expr.indexOf("and");
            int idxOr = bool_expr.indexOf("or");
            int idxOpenBrac = bool_expr.indexOf("(");
            int idxCloseBrac = bool_expr.indexOf(")");
            String type;
            int condition1;
            int condition11;
            int condition2;
            int condition21;
            String combinationType="and";
            if((idxOpenBrac < idxOr && idxOpenBrac > idxAnd && idxCloseBrac > idxOr)||(idxOpenBrac < idxAnd && idxOpenBrac > idxOr && idxCloseBrac > idxAnd)) {
                if (idxAnd > idxOr) {
                    type = "or";
                    condition1 = Integer.valueOf(split[0].strip());
                    condition11 = Integer.valueOf(split[2].strip());
                    condition2 = Integer.valueOf(split[0].strip());
                    condition21 = Integer.valueOf(split[4].strip());
                } else {
                    type = "and";
                    condition1 = Integer.valueOf(split[0].strip());
                    condition11 = Integer.valueOf(split[2].strip());
                    condition2 = Integer.valueOf(split[0].strip());
                    condition21 = Integer.valueOf(split[4].strip());
                    combinationType = "or";
                }
                //  1 AND (2 OR 3) => 1 AND 2 (or) 2 AND 3
                //  1 OR (2 AND 3) => 1 OR 2 (and) 1 OR 3
            }else if((idxOpenBrac < idxOr && idxOpenBrac < idxAnd && idxCloseBrac > idxOr)||(idxOpenBrac < idxAnd && idxOpenBrac < idxOr && idxCloseBrac > idxAnd)){
                if (idxAnd < idxOr) {
                    type = "and";
                    condition1 = Integer.valueOf(split[0].strip());
                    condition11 = Integer.valueOf(split[4].strip());
                    condition2 = Integer.valueOf(split[2].strip());
                    condition21 = Integer.valueOf(split[4].strip());
                    combinationType = "or";
                } else {
                    type = "or";
                    condition1 = Integer.valueOf(split[0].strip());
                    condition11 = Integer.valueOf(split[4].strip());
                    condition2 = Integer.valueOf(split[2].strip());
                    condition21 = Integer.valueOf(split[4].strip());
                }
                //  (1 AND 2) OR 3 => 1 OR 3 (and) 2 OR 3
                //  (1 OR 2) AND 3 => 1 AND 3 (or) 2 AND 3
            }else{
                throw new ExpressionParsingException("Incorrect Order or Content of Conditions");
            }
            BooleanExpression exp1 = new BooleanExpression(condition1, condition11, type, combinationType);
            BooleanExpression exp2 = new BooleanExpression(condition2, condition21, type, combinationType);
            boolEx.add(exp1);
            boolEx.add(exp2);
        }else{
            throw new ExpressionParsingException("Incorrect Order or Content of Conditions");
        }
        return boolEx;
    }

    private static List<BooleanExpression> validateSimpleExpression(String bool_expr) throws ExpressionParsingException {
        List<BooleanExpression> boolEx = new ArrayList<>();
        bool_expr = bool_expr.replace("(", "").replace(")","");
        String type;
        int condition1=-1;
        int condition2=-1;
        int condition3=-1;
        int condition4=-1;
        if(bool_expr.contains("and")){
            type = "and";
            String[] split = bool_expr.split("and");
            int number_conditions = split.length;
            if(number_conditions>4){
                throw new ExpressionParsingException("Please ensure that you used a valid grammar.");
            }
            condition1 = Integer.valueOf(split[0].strip());
            condition2 = Integer.valueOf(split[1].strip());

            if(number_conditions>=3){
                condition3 = Integer.valueOf(split[2].strip());
            }
            if(number_conditions==4){
                condition4 = Integer.valueOf(split[3].strip());
            }

        }else if (bool_expr.contains("or")){
            type = "or";
            String[] split = bool_expr.split("or");
            int number_conditions = split.length;
            if(number_conditions>4){
                throw new ExpressionParsingException("Please ensure that you used a valid grammar.");
            }
            condition1 = Integer.valueOf(split[0].strip());
            condition2 = Integer.valueOf(split[1].strip());

            if(number_conditions>=3){
                condition3 = Integer.valueOf(split[2].strip());
            }
            if(number_conditions==4){
                condition4 = Integer.valueOf(split[3].strip());
            }
        }else{
            type = "and";
            if(bool_expr.strip().split("\\s+").length>1){
                throw new ExpressionParsingException("Please ensure that you used a valid grammar.");
            }
            condition1 = Integer.valueOf(bool_expr.strip());
            condition2 = Integer.valueOf(bool_expr.strip());
        }
        BooleanExpression exp1 = new BooleanExpression(condition1, condition2, condition3, condition4, type, "and");
        boolEx.add(exp1);
        return boolEx;
    }
}