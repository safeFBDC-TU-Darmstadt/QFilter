package user.queryprocessing;

import user.exceptions.QueryProcessingException;
import user.queryparsing.ParsedQuery;
import user.queryparsing.QueryCondition;
import common.Query;
import common.TranslatedQueryCondition;
import common.UnaryTranslator;

import java.util.ArrayList;
import java.util.List;

public abstract class QueryTranslator {

    public static Query translateQueryConditionValues(ParsedQuery query, UnaryTranslator unaryTranslator) throws QueryProcessingException {
        List<TranslatedQueryCondition> translatedConditions = new ArrayList<>();

        if (!unaryTranslator.getUnaryTranslationMeta().containsKey(query.getTable()))
            throw new QueryProcessingException("unknown table '" + query.getTable() + "'.");

        for (QueryCondition queryCondition : query.getConditions()) {
            translatedConditions.add(new TranslatedQueryCondition(queryCondition.getAttributeName(), unaryTranslator.translatePositiveIntegerString(query.getTable(), queryCondition.getValue().replace(".", ""))));
        }

        return new Query(query.getAggregateType(), query.getAttribute(), query.getTable(), query.getConditionalType(), translatedConditions);
    }

}
