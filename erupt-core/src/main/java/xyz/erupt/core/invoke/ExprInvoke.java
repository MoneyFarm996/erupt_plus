package xyz.erupt.core.invoke;

import xyz.erupt.annotation.expr.Expr;
import xyz.erupt.annotation.expr.ExprBool;
import xyz.erupt.annotation.expr.ExprClass;
import xyz.erupt.annotation.expr.ExprInt;
import xyz.erupt.core.util.EruptSpringUtil;

/**
 * @author liyuepeng
 * @date 2021/3/16 13:44
 */
public class ExprInvoke {

    public static String getExpr(Expr expr) {
        String value = expr.value();
        if (!expr.exprHandler().isInterface()) {
            value = EruptSpringUtil.getBean(expr.exprHandler()).handler(value, expr.params());
        }
        return value;
    }

    public static Boolean getExpr(ExprBool expr) {
        boolean value = expr.value();
        if (!expr.exprHandler().isInterface()) {
            value = EruptSpringUtil.getBean(expr.exprHandler()).handler(value, expr.params());
        }
        return value;
    }

    public static int getExpr(ExprInt expr) {
        int value = expr.value();
        if (!expr.exprHandler().isInterface()) {
            value = EruptSpringUtil.getBean(expr.exprHandler()).handler(value, expr.params());
        }
        return value;
    }

    public static Class<?> getExpr(ExprClass expr) {
        Class<?> value = expr.value();
        if (!expr.exprHandler().isInterface()) {
            value = EruptSpringUtil.getBean(expr.exprHandler()).handler(value, expr.params());
        }
        return value;
    }


}
