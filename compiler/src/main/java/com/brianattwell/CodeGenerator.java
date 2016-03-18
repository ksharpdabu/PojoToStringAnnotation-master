package com.brianattwell;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.List;

import static com.squareup.javapoet.ClassName.get;
import static com.squareup.javapoet.MethodSpec.methodBuilder;
import static com.squareup.javapoet.TypeSpec.classBuilder;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

final class CodeGenerator {

    private static final String CLASS_NAME = "StringUtil";

    public static TypeSpec generateClass(List<AnnotatedClass> classes) {

//        使用javapoet库来创建类，设置类的修饰符
        TypeSpec.Builder builder =  classBuilder(CLASS_NAME)
                .addModifiers(PUBLIC, FINAL);

//        有几个添加了注解的类，就要创建几个对应的方法
        for (AnnotatedClass anno : classes) {
            builder.addMethod(makeCreateStringMethod(anno));
        }
        return builder.build();
    }

    /**
     * @return a createString() method that takes annotatedClass's type as an input.
     */
    private static MethodSpec makeCreateStringMethod(AnnotatedClass annotatedClass) {
        StringBuilder builder = new StringBuilder();

//        下面是要创建的方法的return语句
        builder.append(String.format("return \"%s{\" + ", annotatedClass.annotatedClassName));
        for (String variableName : annotatedClass.variableNames) {
            builder.append(String.format(" \"%s='\" + String.valueOf(instance.%s) + \"',\" + ",
                    variableName, variableName));
        }
        builder.append("\"}\"");
        //--创建return语句结束

        return methodBuilder("createString")
                .addJavadoc("@return string suitable for {@param instance}'s toString()")
                .addModifiers(PUBLIC, STATIC)
                .addParameter(get(annotatedClass.getType()), "instance")
                .addStatement(builder.toString())
                .returns(String.class)
                .build();
    }
}
