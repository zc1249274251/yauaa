/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2016 Niels Basjes
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package nl.basjes.parse.useragent.analyze;

import nl.basjes.parse.useragent.UserAgentTreeWalkerBaseVisitor;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.HashMap;
import java.util.Map;

import static nl.basjes.parse.useragent.UserAgentTreeWalkerParser.*;

public class NumberRangeVisitor extends UserAgentTreeWalkerBaseVisitor<NumberRangeList> {

    private static final Integer DEFAULT_MIN = 1;
    private static final Integer DEFAULT_MAX = 10;

    private static Map<String, Integer> MAX_RANGE = new HashMap<>();

    static {
        // Hardcoded maximum values because of the parsing rules
        MAX_RANGE.put("agent"     ,           1);
        MAX_RANGE.put("name"      ,           1);
        MAX_RANGE.put("key"       ,           1);

        // Did statistics on over 200K real useragents from 2015.
        // These are the maximum values from that test set (+ a little margin)
        MAX_RANGE.put("value"     ,           2); // Max was 2
        MAX_RANGE.put("version"   ,           5); // Max was 4
        MAX_RANGE.put("comments"  ,           2); // Max was 2
        MAX_RANGE.put("entry"     ,          20); // Max was much higher
        MAX_RANGE.put("product"   ,          10); // Max was much higher

        MAX_RANGE.put("email"     ,           2);
        MAX_RANGE.put("keyvalue"  ,           3);
        MAX_RANGE.put("text"      ,           8);
        MAX_RANGE.put("url"       ,           2);
        MAX_RANGE.put("uuid"      ,           4);
    }

    private Integer getMaxRange(NumberRangeContext ctx) {
        ParserRuleContext parent = ctx.getParent();
        if (!(parent instanceof StepDownContext)) {
            return DEFAULT_MAX;
        }
        String name = ((StepDownContext) parent).name.getText();
        if (name == null) {
            return DEFAULT_MAX;
        }
        Integer maxRange = MAX_RANGE.get(name);
        if (maxRange == null) {
            return DEFAULT_MAX;
        }
        return maxRange;
    }

    private static final NumberRangeVisitor singleton = new NumberRangeVisitor();

    public static NumberRangeList getList(NumberRangeContext ctx) {
        return singleton.visit(ctx);
    }

    @Override
    public NumberRangeList visitNumberRangeStartToEnd(NumberRangeStartToEndContext ctx) {
        return new NumberRangeList(
                Integer.parseInt(ctx.rangeStart.getText()),
                Integer.parseInt(ctx.rangeEnd.getText()));
    }

    @Override
    public NumberRangeList visitNumberRangeSingleValue(NumberRangeSingleValueContext ctx) {
        int value = Integer.parseInt(ctx.count.getText());
        return new NumberRangeList(value, value);
    }

    @Override
    public NumberRangeList visitNumberRangeAll(NumberRangeAllContext ctx) {
        return new NumberRangeList(DEFAULT_MIN, getMaxRange(ctx));
    }

    @Override
    public NumberRangeList visitNumberRangeEmpty(NumberRangeEmptyContext ctx) {
        return new NumberRangeList(DEFAULT_MIN, getMaxRange(ctx));
    }
}
