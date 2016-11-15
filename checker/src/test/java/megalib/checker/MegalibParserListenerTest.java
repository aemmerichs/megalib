package test.java.megalib.checker;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.java.megalib.checker.services.MegaModelLoader;
import org.java.megalib.models.Function;
import org.java.megalib.models.MegaModel;
import org.java.megalib.models.Relation;
import org.junit.Test;

/**
 * Tests the correct behavior for MegaModelLoader and MegaModel functionality.
 * Especially the constraints relevant for creation time are tested in a black box manner.
 * 
 * @author heinz
 *
 */
public class MegalibParserListenerTest {
	
	@Test
	public void preludeIsParsed(){
		MegaModelLoader ml = new MegaModelLoader();
		MegaModel model = ml.getModel();
		model.getCriticalWarnings().forEach(w->System.out.println(w));
		assertEquals(19,model.getInstanceOfMap().size());
		assertEquals(19,model.getLinkMap().size());
		assertEquals(29,model.getSubtypesMap().size());
		Map<String, Set<Relation>> rm = model.getRelationshipDeclarationMap();
		//the number of distinct relation ship names
		assertEquals(14,rm.size());
		int count = rm.values().stream().map(set -> set.size()).reduce(0, (a,b) -> a+b);
		assertEquals(42, count);
		assertEquals(0,model.getCriticalWarnings().size());
	}
	
	@Test
	public void addSubtypeInvalidSupertype() {
		String input = "DerivedType < Type";
		MegaModel m = new MegaModelLoader().loadString(input);
		Map<String, String> subtypes = m.getSubtypesMap();
		
		assertFalse(subtypes.containsKey("DerivedType"));
		assertTrue(m.getCriticalWarnings().contains("Error at DerivedType: The declared supertype is not a subtype of Entity"));
		assertEquals(1,m.getCriticalWarnings().size());
	}
	
	@Test
	public void enterEntityInstanceFillsEntityInstances(){
		String input = "Instance : Type";
		MegaModel model = new MegaModelLoader().loadString(input);
		Map<String, String> imap = model.getInstanceOfMap();
		
		assertTrue(imap.containsKey("Instance"));
		assertEquals(imap.get("Instance").toString(),"Type");
		Map<String, Set<Relation>> emap = model.getRelationshipInstanceMap();
		assertTrue(emap.isEmpty());
	}
	
	@Test
	public void enterArtifactInstance(){
		String input = "a : Artifact<Python,?r,?m>";
		MegaModel model = new MegaModelLoader().loadString(input);
		Map<String, String> imap = model.getInstanceOfMap();
		assertEquals(21,imap.size());
		assertTrue(imap.containsKey("a"));
		assertEquals(imap.get("a").toString(),"Artifact");
		Set<Relation> elementOfSet = model.getRelationshipInstanceMap().get("elementOf");
		assertEquals(1,elementOfSet.size());
		Relation elementOf = elementOfSet.iterator().next();
		assertEquals("a",elementOf.getSubject());
		assertEquals("Python",elementOf.getObject());
	}
	
	@Test
	public void enterRelationDeclarationFillsRelationDeclarationsWithRelationName()  {
		String input = "Relation < TypeOne # TypeTwo";
		Map<String, Set<Relation>> actual = new MegaModelLoader().loadString(input).getRelationshipDeclarationMap();
		
		assertTrue(actual.containsKey("Relation"));		
	}
	
	@Test
	public void enterRelationDeclarationFillsRelationDeclarationsWithTypes()  {
		String input = "Relation < TypeOne # TypeTwo";
		Map<String, Set<Relation>> actual = new MegaModelLoader().loadString(input).getRelationshipDeclarationMap();
		
		Set<Relation> types = actual.get("Relation");
		
		assertTrue(types.contains(new Relation("TypeOne","TypeTwo")));
	}
	
	@Test
	public void enterRelationDeclarationDoesNotOverideExistingDeclarations()  {
		String input = "Relation < TypeOne # TypeTwo\nRelation < TypeThree # TypeFour";
		Map<String, Set<Relation>> actual = new MegaModelLoader().loadString(input).getRelationshipDeclarationMap();
		
		Set<Relation> types = actual.get("Relation");
		String[] expected1 = {"TypeOne","TypeTwo"};
		String[] expected2 = {"TypeThree","TypeFour"};
		
		assertTrue(types.contains(Arrays.asList(expected1)));
		assertTrue(types.contains(Arrays.asList(expected2)));
	}
	
	@Test
	public void enterRelationInstanceFillsRelationInstancesWithRelationName()  {
		String input = "ObjectOne Relation ObjectTwo";
		Map<String, Set<Relation>> actual = new MegaModelLoader().loadString(input).getRelationshipInstanceMap();
		
		assertTrue(actual.containsKey("Relation"));
	}
	
	@Test
	public void enterRelationInstanceFillsRelationInstancesWithObjects()  {
		String input = "ObjectOne Relation ObjectTwo";
		Map<String, Set<Relation>> actual = new MegaModelLoader().loadString(input).getRelationshipInstanceMap();
		
		Set<Relation> types = actual.get("Relation");
		
		assertTrue(types.contains(new Relation("ObjectOne","ObjectTwo")));
	}
	
	@Test
	public void enterRelationInstanceDoesNotOverideExistingInstances()  {
		String input = "ObjectOne Relation ObjectTwo\nObjectThree Relation ObjectFour";
		Map<String, Set<Relation>> actual = new MegaModelLoader().loadString(input).getRelationshipInstanceMap();
		
		Set<Relation> types = actual.get("Relation");
		
		assertTrue(types.contains(new Relation("ObjectOne","ObjectTwo")));
		assertTrue(types.contains(new Relation("ObjectThree","ObjectFour")));
	}
	
	@Test
	public void enterFunctionDeclarationFillsFunctionDeclarationsWithFunctionName()  {
		String input = "function : TypeOne # TypeTwo -> ReturnType";
		Map<String, Function> actual = new MegaModelLoader().loadString(input).getFunctionDeclarations();
		
		assertTrue(actual.containsKey("function"));
	}
	
	@Test
	public void enterFunctionInstanceFillsFunctionInstanceWithFunctionName()  {
		String input = "Function(ObjectOne , ObjectTwo) |-> Result";
		Map<String, Set<Function>> actual = new MegaModelLoader().loadString(input).getFunctionApplications();
		
		assertTrue(actual.containsKey("Function"));
	}
	
	@Test
	public void enterFunctionInstanceFillsFunctionInstanceWithFunction()  {
		String input = "Function(ObjectOne , ObjectTwo) |-> Result";
		Map<String, Set<Function>> actual = new MegaModelLoader().loadString(input).getFunctionApplications();
		
		Set<Function> functions = actual.get("Function");
		
		assertEquals(1,functions.size());
	}
	
	@Test
	public void enterFunctionInstanceDoesNotOverideExistingInstances()  {
		String input = "Function(ObjectOne , ObjectTwo) |-> Result\nFunction(ObjectThree , ObjectFour) |-> ResultTwo";
		Map<String, Set<Function>> actual = new MegaModelLoader().loadString(input).getFunctionApplications();
		
		Collection<Function> types = actual.get("Function");
		
		assertEquals(2,types.size());
	}
	
	@Test
	public void enterLinkFillsLinksWithName()  {
		String input = "Name = \"test\"";
		Map<String, List<String>> actual = new MegaModelLoader().loadString(input).getLinkMap();
		
		assertTrue(actual.containsKey("Name"));
	}
	
	@Test
	public void enterLinkFillsLinksWithLinkString()  {
		String input = "Name = \"test\"";
		Map<String, List<String>> actual = new MegaModelLoader().loadString(input).getLinkMap();
		
		String link = actual.get("Name").get(0);
		assertEquals("test", link);
	}
	
	@Test
	public void enterLinkFillsLinksDoesNotOverride()  {
		String input = "Name = \"test\"\nName = \"testTwo\"";
		Map<String, List<String>> actual = new MegaModelLoader().loadString(input).getLinkMap();
		
		assertEquals(2,actual.get("Name").size());
		String link = actual.get("Name").get(0);
		String link2 = actual.get("Name").get(1);
		
		assertEquals("test", link);
		assertEquals("testTwo", link2);
	}
	
	@Test
	public void fileNotFoundReturnsNull() throws IOException{
		MegaModelLoader ml = new MegaModelLoader();
		ml.loadFile("");
		assertNull(ml.getModel());
	}
	
	@Test
	public void testSyntacticallyInvalidString(){
		String input = "xy test";
		assertNull(new MegaModelLoader().loadString(input));
	}	
	
	@Test
	public void testComment() throws IOException{
		String input = "// test hello world";
		MegaModel actual = new MegaModelLoader().loadString(input);
		assertNotNull(actual);
	}
}