package com.besmartmobile.union.processor;


import com.besmartmobile.union.lib.Function;
import com.besmartmobile.union.lib.UnionWithClassInfo;
import com.besmartmobile.union.lib.annotations.UnionAnnotation;
import com.besmartmobile.union.lib.annotations.UnionPackage;
import com.google.auto.service.AutoService;
import com.google.common.collect.Sets;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;

import static javax.lang.model.SourceVersion.latestSupported;

@AutoService(Processor.class)
public class UnionProcessor extends AbstractProcessor {

    private static final String UNION_EXT_CLASS_POSTFIX = "Ext";
    public static final String UNION_CLASSES_FIELD_NAME = "unionClassWithClassInfoClasses";
    private final List<TypeElement> unionTypes = new ArrayList<>();
    private static final String CLASS_NAME_UNION_INFO = "$UnionInfo";


    public static final String MATCH_METHOD_NAME = "match";
    public static final String TO_MATCH_PARAMETER_NAME = "toMatch";
    private String unionPackage;
    private int round = -1;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
    }

    @Override
    public Set getSupportedAnnotationTypes() {
        return Sets.newHashSet(UnionAnnotation.class.getCanonicalName(),
                UnionPackage.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        round++;


        if (round == 0) {
            EnvironmentUtil.init(processingEnv);
            if (!processUnionPackage(roundEnvironment)) {
                return false;
            }
        }

        if (!processAnnotations(roundEnvironment)) {
            return false;
        }

        if (roundEnvironment.processingOver()) {
            return true;
        }

        return false;
    }

    private boolean processAnnotations(RoundEnvironment roundEnv) {
        return processUnions(roundEnv);
    }

    private boolean processUnionPackage(RoundEnvironment roundEnv) {
//        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
//                "processVmProviderPackage");
        final Set<? extends Element> elements = roundEnv
                .getElementsAnnotatedWith(UnionPackage.class);

        if (elements == null || elements.size() != 1) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Should have single package annotated with @UnionPackage");
            return false;
        }

        Element element = elements.iterator().next();

        if (element.getKind() != ElementKind.PACKAGE) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Only package annotated with @VmProviderPackage, not " + element.getKind());
            return false;
        }


        Name packageName = ((PackageElement) element).getQualifiedName();

        this.unionPackage = packageName.toString();

        return true;
    }
//
    private boolean processUnions(RoundEnvironment roundEnv) {
        final Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(UnionAnnotation.class);

        if (elements == null || elements.isEmpty()) {
            return true;
        }

        for (Element element : elements) {
            if (element.getKind() != ElementKind.CLASS
                    || !element.getModifiers().contains(Modifier.ABSTRACT)) {
                EnvironmentUtil.logError("@UnionAnnotation can only be used for abstract classes.");
                return false;
            }

            try {
                generateUnionExt((TypeElement) element);
            } catch (IOException e) {
                EnvironmentUtil.logError(e.getMessage());
                return false;
            }

//            final TypeMirror annotationTypeMirror = element.asType();
//            final ClassName annotationTypeName = (ClassName) ClassName.get(annotationTypeMirror);

//            EnvironmentUtil.logWarning("" + annotationTypeName.toString());

//            if (!generateGetMethodAndFactory((TypeElement) element)) {
//                return false;
//            }
        }

        if (!generateUnionInfo()) {
            return false;
        }

        return true;
    }

    private boolean generateUnionInfo() {

        final TypeSpec.Builder builder = TypeSpec.classBuilder(CLASS_NAME_UNION_INFO);
        builder.addModifiers(Modifier.PUBLIC, Modifier.FINAL);


        ParameterizedTypeName unionClassesType = ParameterizedTypeName.get(
                ClassName.get(List.class),
                ClassName.get(Class.class));
        MethodSpec.Builder getUnionClassesMethodSpecBuilder = MethodSpec.methodBuilder("get" + UNION_CLASSES_FIELD_NAME)
                .returns(unionClassesType)
                .addStatement("return " + UNION_CLASSES_FIELD_NAME)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC);

        FieldSpec.Builder unionClassesMethodSpecBuilder = FieldSpec.builder(
                unionClassesType, UNION_CLASSES_FIELD_NAME, Modifier.PRIVATE, Modifier.STATIC);



        ParameterizedTypeName arrayListOfClassesType = ParameterizedTypeName.get(
                ClassName.get(ArrayList.class),
                ClassName.get(Class.class));

        CodeBlock.Builder staticInitializerBlockBuilder = CodeBlock.builder();

        staticInitializerBlockBuilder.addStatement(UNION_CLASSES_FIELD_NAME + " = "
                + "new $T()", arrayListOfClassesType);

        staticInitializerBlockBuilder.beginControlFlow("try");
        for (TypeElement unionType : unionTypes) {
            final TypeMirror unionTypeSuperclassMirror = unionType.getSuperclass();
            final ClassName unionTypeSuperclassName = (ClassName) ClassName.get(unionTypeSuperclassMirror);
            if (UnionWithClassInfo.class.getCanonicalName().equals(unionTypeSuperclassName.toString())) {
                staticInitializerBlockBuilder.addStatement(
                        UNION_CLASSES_FIELD_NAME + ".add(Class.forName($S))",
                        unionType.getQualifiedName().toString());
            }
        }
        staticInitializerBlockBuilder.nextControlFlow("catch (ClassNotFoundException e)");
        staticInitializerBlockBuilder.addStatement("throw new IllegalStateException()");
        staticInitializerBlockBuilder.endControlFlow();


        builder.addField(unionClassesMethodSpecBuilder.build());
        builder.addMethod(getUnionClassesMethodSpecBuilder.build());
        builder.addStaticBlock(staticInitializerBlockBuilder.build());

        TypeSpec typeSpec = builder.build();


        JavaFile javaFile = JavaFile.builder(unionPackage, typeSpec)
                .build();

//        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "before generateFile");


        try {
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }


        return false;
    }

    private void generateUnionExt(TypeElement element) throws IOException {
        unionTypes.add(element);

        final TypeMirror unionTypeMirror = element.asType();
        final ClassName unionTypeName = (ClassName) ClassName.get(unionTypeMirror);




        final TypeSpec.Builder builder = TypeSpec.classBuilder(unionTypeName.simpleName() + UNION_EXT_CLASS_POSTFIX);
        builder.addModifiers(Modifier.PUBLIC, Modifier.FINAL);


        List<TypeElement> unionSubtypes = ElementFilter.typesIn(element.getEnclosedElements());

        for (TypeElement unionSubtype : unionSubtypes) {
            Name qualifiedName = unionSubtype.getQualifiedName();
//            EnvironmentUtil.logWarning("" + qualifiedName);



            ArrayList<ParameterSpec> parameterSpecs = new ArrayList<>();
            List<VariableElement> fieldElements = ElementFilter.fieldsIn(unionSubtype.getEnclosedElements());
            for (VariableElement parameter : fieldElements) {
                TypeName parameterTypeName = ClassName.get(parameter.asType());
                Name parameterName = parameter.getSimpleName();

//                processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, parameterName + " " + parameterTypeName.toString());
                ParameterSpec parameterSpec = ParameterSpec.builder(parameterTypeName, parameterName.toString(), Modifier.FINAL)
                        .build();
                parameterSpecs.add(parameterSpec);
            }
            String parametersCallBlock = getParametersCallBlock(parameterSpecs);




            String factoryMethodName = getUnionSubtypeNameInCamelCase(unionSubtype);

            MethodSpec.Builder factoryMethodSpecBuilder = MethodSpec.methodBuilder(factoryMethodName)
                    .addParameters(parameterSpecs)
//                    .addTypeVariable(createMethodTypeVariable)
                    .returns(ClassName.get(unionTypeMirror))
                    .addStatement("return new $T($L)", unionSubtype.asType(), parametersCallBlock)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC);

            MethodSpec factoryCreateMethodSpec = factoryMethodSpecBuilder.build();

            builder.addMethod(factoryCreateMethodSpec);
        }



        // match method generation

        TypeVariableName matchMethodTypeVariable = TypeVariableName.get("T");

        ParameterSpec unionParameterSpec = ParameterSpec.builder(unionTypeName,
                TO_MATCH_PARAMETER_NAME,
                Modifier.FINAL)
                .build();

        ArrayList<ParameterSpec> parameterSpecs = new ArrayList<>();
        parameterSpecs.add(unionParameterSpec);

        for (TypeElement unionSubtype : unionSubtypes) {
            TypeName parameterTypeName = ClassName.get(unionSubtype.asType());

            ParameterizedTypeName matchFunctionClassTypeName = ParameterizedTypeName.get(
                    ClassName.get(Function.class),
                    parameterTypeName,
                    matchMethodTypeVariable);


            ParameterSpec parameterSpec = ParameterSpec.builder(matchFunctionClassTypeName,
                    getMatchFunctionParameterName(unionSubtype),
                    Modifier.FINAL)
                    .build();
            parameterSpecs.add(parameterSpec);
        }

        MethodSpec.Builder matchMethodSpecBuilder = MethodSpec.methodBuilder(MATCH_METHOD_NAME)
                .addParameters(parameterSpecs)
                .addTypeVariable(matchMethodTypeVariable)
                .returns(matchMethodTypeVariable)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC);

        for (TypeElement unionSubtype : unionSubtypes) {
            matchMethodSpecBuilder.beginControlFlow(
                    "if(" + TO_MATCH_PARAMETER_NAME + " instanceof $T)", unionSubtype.asType());
            matchMethodSpecBuilder.addStatement("return "
                    + getMatchFunctionParameterName(unionSubtype)
                    + ".apply(($T) " + TO_MATCH_PARAMETER_NAME + ")", unionSubtype.asType());
            matchMethodSpecBuilder.endControlFlow();
        }
        matchMethodSpecBuilder.addStatement("throw new IllegalStateException()");

        MethodSpec matchMethodSpec = matchMethodSpecBuilder.build();

        builder.addMethod(matchMethodSpec);






        TypeSpec typeSpec = builder.build();


        JavaFile javaFile = JavaFile.builder(unionTypeName.packageName(), typeSpec)
                .build();

        javaFile.writeTo(processingEnv.getFiler());
    }

    private String getUnionSubtypeNameInCamelCase(TypeElement unionSubtype) {
        String unionSubtypeSimpleName = unionSubtype.getSimpleName().toString();
        String firstSymbolOfUnionSubtypeSimpleName = "" + unionSubtypeSimpleName.charAt(0);
        return firstSymbolOfUnionSubtypeSimpleName.toLowerCase()
                + unionSubtypeSimpleName.substring(1);
    }

    private String getMatchFunctionParameterName(TypeElement unionSubtype) {
        String unionSubtypeNameInCamelCase = getUnionSubtypeNameInCamelCase(unionSubtype);
        return unionSubtypeNameInCamelCase + "Function";
    }

    private String getParametersCallBlock(ArrayList<ParameterSpec> parameterSpecs) {
        StringBuilder parametersCallBlockStringBuilder = new StringBuilder();
        for (int i = 0; i < parameterSpecs.size(); i++) {
            ParameterSpec parameterSpec = parameterSpecs.get(i);
            if (i == parameterSpecs.size() - 1) {
                parametersCallBlockStringBuilder.append(parameterSpec.name);
            } else {
                parametersCallBlockStringBuilder.append(parameterSpec.name);
                parametersCallBlockStringBuilder.append(", ");
            }
        }
        return parametersCallBlockStringBuilder.toString();
    }

}
