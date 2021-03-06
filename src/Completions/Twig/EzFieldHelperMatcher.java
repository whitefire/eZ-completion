package Completions.Twig;

import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.ProcessingContext;
import com.jetbrains.twig.TwigTokenTypes;
import com.jetbrains.twig.elements.TwigElementTypes;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Attempts to match the ContentType by using twig-comments.
 */
public class EzFieldHelperMatcher extends PatternCondition<PsiElement>
{
    protected static List<String> allowedTypes = new ArrayList<>(Arrays.asList(
        "ez_field_value",
        "ez_render_field",
        "ez_field_description",
        "ez_field_name",
        "ez_is_field_empty",
        "ez_field"
    ));

    protected static List<String> allowContentMethods = new ArrayList<>(Arrays.asList(
        "getField",
            "field",
            "getFieldValue",
            "fieldValue"
    ));

    public EzFieldHelperMatcher() { super("EzFieldHelper"); }

    @Override
    public boolean accepts(@NotNull PsiElement psiElement, ProcessingContext processingContext)
    {
        LeafPsiElement source;
        try {
            source = (LeafPsiElement)psiElement;
        }
        catch (Exception e) {
            return false;
        }

        if (!withinQuotes(source)) {
            return false;
        }

        PsiElement context = psiElement.getContext();
        if (context == null) {
            return false;
        }

        boolean correctContext = PlatformPatterns.psiElement().withElementType(PlatformPatterns.elementType().or(
            TwigElementTypes.PRINT_BLOCK,
            TwigElementTypes.IF_TAG,
            TwigElementTypes.SET_TAG
        )).accepts(context);

        if (!correctContext) {
            return false;
        }

        LeafPsiElement variable = findPreviousSibling(source, TwigTokenTypes.IDENTIFIER, null);
        LeafPsiElement identifierStart;
        try {
            identifierStart = (LeafPsiElement)variable.getPrevSibling();
        }
        catch (Exception e) {
            return false;
        }

        PsiElement twigHelper = findPreviousSibling(identifierStart, TwigTokenTypes.IDENTIFIER, null);
        if (twigHelper == null) {
            return false;
        }

        if (allowedTypes.contains(twigHelper.getText())) {
            return matchContentType(psiElement, variable.getText(), processingContext);
        }
        else if (allowContentMethods.contains(variable.getText())) {
            return matchContentType(psiElement, twigHelper.getText(), processingContext);
        }

        return false;
    }

    protected Boolean matchContentType(PsiElement psiElement, String variableName, ProcessingContext context)
    {
        // Attempt to find comment describing type.
        CommentMatcher commentMatcher = new CommentMatcher(variableName);
        psiElement.getContainingFile().accept(commentMatcher);
        String contentTypeIdentifier = commentMatcher.getMatch();
        if (contentTypeIdentifier == null) {
            return false;
        }

        context.put("contentTypeIdentifier", contentTypeIdentifier);
        return true;
    }


    protected boolean withinQuotes(LeafPsiElement psiElement) {
        return (
            findPreviousSibling(psiElement, null, "'") != null &&
            findNextSibling(psiElement, null, "'") != null
        ) || (
            findPreviousSibling(psiElement, null, "\"") != null &&
            findNextSibling(psiElement, null, "\"") != null
        );
    }

    protected LeafPsiElement findPreviousSibling(LeafPsiElement source, IElementType elementType, String value)
    {
        if (source == null) {
            return null;
        }

        try {
            return sourceMatches(source, elementType, value)
                ? source
                : findPreviousSibling((LeafPsiElement) source.getPrevSibling(), elementType, value);
        }
        catch (Exception e) {
            return null;
        }
    }

    protected LeafPsiElement findNextSibling(LeafPsiElement source, IElementType elementType, String value)
    {
        if (source == null) {
            return null;
        }

        try {
            return sourceMatches(source, elementType, value)
                ? source
                : findNextSibling((LeafPsiElement) source.getNextSibling(), elementType, value);
        }
        catch (Exception e) {
            return null;
        }
    }

    protected boolean sourceMatches(LeafPsiElement source, IElementType elementType, String value)
    {
        if (source.getElementType() == elementType && value == null) {
            return true;
        }

        if (elementType == null && source.getText().equals(value)) {
            return true;
        }

        return source.getElementType() == elementType && source.getText().equals(value);
    }
}