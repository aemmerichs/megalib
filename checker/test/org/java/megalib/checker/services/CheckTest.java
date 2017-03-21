package org.java.megalib.checker.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.java.megalib.checker.services.WellformednessCheck;
import org.java.megalib.checker.services.ModelLoader;
import org.java.megalib.models.MegaModel;
import org.java.megalib.parser.ParserException;
import org.junit.Test;


public class CheckTest {

    @Test
    public void checkPrelude() {
        ModelLoader ml = new ModelLoader();
        assertEquals(0, ml.getTypeErrors().size());
        MegaModel m = ml.getModel();
        WellformednessCheck c = new WellformednessCheck(m);
        assertEquals(0, c.getWarnings().size());
    }

    @Test
    public void checkInstanceOfTechnology() throws ParserException, IOException {
        ModelLoader ml = new ModelLoader();
        String input = "/**/?t : Technology. " + "?l : ProgrammingLanguage. " + "?t uses ?l.";
        ml.loadString(input);
        assertEquals(0, ml.getTypeErrors().size());
        WellformednessCheck c = new WellformednessCheck(ml.getModel(), true);
        assertEquals(0, c.getWarnings().size());
    }

    @Test
    public void checkInstanceOfTechnologyUnderspec() throws ParserException, IOException {
        ModelLoader ml = new ModelLoader();
        String input = "/**/t : Technology;=\"http://softlang.org/\". " + "?l : ProgrammingLanguage. " + "t uses ?l.";
        ml.loadString(input);
        assertEquals(0, ml.getTypeErrors().size());
        WellformednessCheck c = new WellformednessCheck(ml.getModel(), true);
        assertEquals(1, c.getWarnings().size());
        assertEquals("State a specific subtype of Technology for t.", c.getWarnings().get(0));
    }

    @Test
    public void checkInstanceOfLanguage() throws ParserException, IOException {
        ModelLoader ml = new ModelLoader();
        String input = "/**/?t : Library. " + "?l : Language. " + "?t uses ?l.";
        ml.loadString(input);
        assertEquals(0, ml.getTypeErrors().size());
        WellformednessCheck c = new WellformednessCheck(ml.getModel(), true);
        assertEquals(0, ml.getTypeErrors().size());
        assertEquals(0, c.getWarnings().size());
    }

    @Test
    public void checkInstanceOfLanguageUnderspec() throws ParserException, IOException {
        ModelLoader ml = new ModelLoader();
        String input = "/**/?t : Library. " + "l : Language;=\"http://softlang.org/\";^uses ?t;^implements ?t.";
        ml.loadString(input);
        assertEquals(0, ml.getTypeErrors().size());
        WellformednessCheck c = new WellformednessCheck(ml.getModel(), true);
        assertEquals(0, ml.getTypeErrors().size());
        assertEquals(1, c.getWarnings().size());
        assertTrue(c.getWarnings()
                    .contains("State a specific subtype of Language for l."));
    }

    @Test
    public void checkLinkExistence() throws ParserException, IOException {
        ModelLoader ml = new ModelLoader();
        String input = "/**/t : Library. " + "?l : ProgrammingLanguage. " + "t uses ?l.";
        ml.loadString(input);
        assertEquals(0, ml.getTypeErrors().size());
        WellformednessCheck c = new WellformednessCheck(ml.getModel(), true);

        assertEquals(1, c.getWarnings().size());
        assertEquals("Link missing for entity t.", c.getWarnings().get(0));
    }

    @Test
    public void checkTechnologyUsesLanguage() throws ParserException, IOException {
        ModelLoader ml = new ModelLoader();
        String input = "/**/t : Library; = \"http://softlang.wikidot.com/\".";
        ml.loadString(input);
        WellformednessCheck c = new WellformednessCheck(ml.getModel(), true);

        assertEquals(0, ml.getTypeErrors().size());
        assertEquals(1, c.getWarnings().size());
        assertEquals("State a used language for t", c.getWarnings().get(0));
    }

    @Test
    public void checkFunctionImplementation() throws ParserException, IOException {
        ModelLoader ml = new ModelLoader();
        String input = "/**/?l : ProgrammingLanguage. " + "f : ?l -> ?l. " + "?a : Artifact. " + "?a elementOf ?l. "
                       + "?a hasRole MvcModel." + "?a manifestsAs File. " + "f(?a)|->?a.";
        ml.loadString(input);
        WellformednessCheck c = new WellformednessCheck(ml.getModel(), true);

        assertEquals(0, ml.getTypeErrors().size());
        c.getWarnings().forEach(w -> System.out.println(w));
        assertEquals(1, c.getWarnings().size());
        assertTrue(c.getWarnings().contains("The function f is not implemented. Please state what implements it."));
    }

    @Test
    public void checkFunctionApplication() throws ParserException, IOException {
        ModelLoader ml = new ModelLoader();
        String input = "/**/?l : ProgrammingLanguage. " + "f : ?l -> ?l. " + "?a : Artifact. " + "?a elementOf ?l. "
                       + "?a hasRole MvcModel. " + "?a manifestsAs File. " + "?t : Library. " + "?t uses ?l. "
                       + "?t implements f.";
        ml.loadString(input);
        WellformednessCheck c = new WellformednessCheck(ml.getModel(), true);
        assertEquals(0, ml.getTypeErrors().size());
        assertEquals(1, c.getWarnings().size());
        assertTrue(c.getWarnings().contains("The function f is not applied yet. Please state an actual application."));
    }

    @Test
    public void checkArtifactElementOf() throws ParserException, IOException {
        ModelLoader ml = new ModelLoader();
        String input = "/**/?a : Artifact. " + "?a hasRole MvcModel. " + "?a manifestsAs File.";
        ml.loadString(input);
        WellformednessCheck c = new WellformednessCheck(ml.getModel(), true);
        assertEquals(0, ml.getTypeErrors().size());
        assertEquals(1, c.getWarnings().size());
        assertTrue(c.getWarnings().contains("Language missing for artifact ?a"));
    }

    @Test
    public void checkArtifactManifestsAs() throws ParserException, IOException {
        ModelLoader ml = new ModelLoader();
        String input = "/**/?l : ProgrammingLanguage. " + "?a : Artifact. " + "?a elementOf ?l. "
                       + "?a hasRole MvcModel. ";
        ml.loadString(input);
        WellformednessCheck c = new WellformednessCheck(ml.getModel(), true);
        assertEquals(0, ml.getTypeErrors().size());
        assertEquals(1, c.getWarnings().size());
        assertTrue(c.getWarnings().contains("Manifestation missing for ?a"));
    }

    @Test
    public void checkAbstractArtifactHasRole() throws ParserException, IOException {
        ModelLoader ml = new ModelLoader();
        String input = "/**/?l : ProgrammingLanguage. " + "?a : Artifact. " + "?a elementOf ?l. "
                       + "?a manifestsAs File.";
        ml.loadString(input);
        WellformednessCheck c = new WellformednessCheck(ml.getModel(), true);
        assertEquals(0, ml.getTypeErrors().size());
        assertEquals(0, c.getWarnings().size());
    }

    @Test
    public void checkArtifactHasRole() throws ParserException, IOException {
        ModelLoader ml = new ModelLoader();
        String input = "/**/?l : ProgrammingLanguage. a : Artifact; ~= \"http://softlang.org/\". a elementOf ?l. a manifestsAs File.";
        ml.loadString(input);
        WellformednessCheck c = new WellformednessCheck(ml.getModel(), true);
        assertEquals(0, ml.getTypeErrors().size());
        c.getWarnings().forEach(w -> System.out.println(w));
        assertEquals(1, c.getWarnings().size());
        assertEquals("Role missing for a.", c.getWarnings().get(0));
    }

    @Test
    public void checkCyclicSubsets() throws ParserException, IOException {
        ModelLoader ml = new ModelLoader();
        String input = "/**/?l1 : ProgrammingLanguage. " + "?l2 : ProgrammingLanguage. " + "?l3 : ProgrammingLanguage. "
                       + "?l1 subsetOf ?l2. " + "?l2 subsetOf ?l3. " + "?l3 subsetOf ?l2.";
        ml.loadString(input);
        WellformednessCheck c = new WellformednessCheck(ml.getModel(), true);
        assertEquals(0, ml.getTypeErrors().size());
        c.getWarnings().forEach(w -> System.out.println(w));
        assertEquals(1, c.getWarnings().size());
        assertTrue(c.getWarnings()
                   .contains("Cycles exist concerning the relationship subsetOf involving the following entities :[?l2, ?l3]"));
    }

    @Test
    public void checkLinkNotWorking() throws ParserException, IOException {
        ModelLoader ml = new ModelLoader();
        String input = "/**/?l : ProgrammingLanguage; " + " = \"http://www.nowebsitehere.de\".";
        ml.loadString(input);
        WellformednessCheck c = new WellformednessCheck(ml.getModel());
        assertEquals(0, ml.getTypeErrors().size());
        assertEquals(1, c.getWarnings().size());
        assertTrue(c.getWarnings().contains("Error at Link to 'http://www.nowebsitehere.de' : Connection failed!"));
    }
}
