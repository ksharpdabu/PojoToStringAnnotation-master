package com.brianattwell;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.*;
import javax.annotation.processing.Messager;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import static com.brianattwell.Utils.getPackageName;
import static com.squareup.javapoet.JavaFile.builder;
import static java.util.Collections.singleton;
import static javax.lang.model.SourceVersion.latestSupported;
import static javax.tools.Diagnostic.Kind.*;

@AutoService(Processor.class)
public class StaticStringUtilProcessor extends AbstractProcessor {

    private static final String ANNOTATION = "@" + StaticStringUtil.class.getSimpleName();

    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        //singleton是一个不可变的set，值包含特定的对象
        return singleton(StaticStringUtil.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        ArrayList<AnnotatedClass> annotatedClasses = new ArrayList<>();
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(StaticStringUtil.class)) {
            // Our annotation is defined with @Target(value=TYPE). Therefore, we can assume that
            // this annotatedElement is a TypeElement.
            //因为我们的注解被定义为@Target(value=TYPE)。所以，我们可以假设这个annotatedelement
            //是TypeElement
            TypeElement annotatedClass = (TypeElement) annotatedElement;
            if (!isValidClass(annotatedClass)) {
                return true;
            }


            try {
                annotatedClasses.add(buildAnnotatedClass(annotatedClass));
            } catch (NoPackageNameException | IOException e) {
                String message = String.format("Couldn't process class %s: %s", annotatedClass,
                        e.getMessage());
                messager.printMessage(ERROR, message, annotatedElement);
            }
        }
        try {
            generate(annotatedClasses);
        } catch (NoPackageNameException | IOException e) {
            messager.printMessage(ERROR, "Couldn't generate class");
        }
        return true;
    }


    //调用ClassValidator的两个静态方法来检测，添加了注解的类是否是public且非Abstract
    private boolean isValidClass(TypeElement annotatedClass) {

        if (!ClassValidator.isPublic(annotatedClass)) {
            String message = String.format("Classes annotated with %s must be public.",
                    ANNOTATION);
            messager.printMessage(ERROR, message, annotatedClass);
            return false;
        }

        if (ClassValidator.isAbstract(annotatedClass)) {
            String message = String.format("Classes annotated with %s must not be abstract.",
                    ANNOTATION);
            messager.printMessage(ERROR, message, annotatedClass);
            return false;
        }

        return true;
    }


//    在添加了注解的类中，获取变量名，并将其保存到一个ArrayList中
    private AnnotatedClass buildAnnotatedClass(TypeElement annotatedClass)
            throws NoPackageNameException, IOException {
        ArrayList<String> variableNames = new ArrayList<>();
        for (Element element : annotatedClass.getEnclosedElements()) {
            if (!(element instanceof VariableElement)) {
                continue;
            }
            VariableElement variableElement = (VariableElement) element;
            variableNames.add(variableElement.getSimpleName().toString());
        }
        return new AnnotatedClass(annotatedClass , variableNames);
    }

    private void generate(List<AnnotatedClass> annos) throws NoPackageNameException, IOException {
        if (annos.size() == 0) {
            return;
        }
//        processingEnv继承自抽象类的
        String packageName = getPackageName(processingEnv.getElementUtils(),
                annos.get(0).typeElement);
        TypeSpec generatedClass = CodeGenerator.generateClass(annos);

        JavaFile javaFile = builder(packageName, generatedClass).build();
        javaFile.writeTo(processingEnv.getFiler());
    }
}

