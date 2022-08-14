package com.verizon.mastars2k.conflictmanagement.rule.transport;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.mastars.servicecore.client.MastarsClientFactory;
import com.mastars.servicecore.dataaccess.DataBaseAccess;
import com.mastars.servicecore.dataaccess.MastarsDataBaseAccessFactory;
import com.mastars.vo.obj.Item;
import com.mastars.vo.obj.Event;
import com.mastars.vo.obj.TirksCableEquipment;
import com.mastars.vo.obj.TirksDXCEquipment;
import com.mastars.vo.obj.TirksSCIDEquipment;
import com.mastars.vo.obj.impl.DDCEquipmentImpl;
import com.mastars.vo.obj.impl.DataEquipmentImpl;
import com.mastars.vo.obj.impl.DefaultEquipmentImpl;
import com.mastars.vo.obj.impl.DxcEquipmentImpl;
import com.mastars.vo.obj.impl.EtsEquipmentImpl;
import com.mastars.vo.obj.impl.FIOSEquipmentImpl;
import com.mastars.vo.obj.impl.FiberCableEquipmentImpl;
import com.mastars.vo.obj.impl.IconEquipmentImpl;
import com.mastars.vo.obj.impl.IsnServerItemImpl;
import com.mastars.vo.obj.impl.ItemImpl;
import com.mastars.vo.obj.impl.MeccaEquipmentImpl;
import com.mastars.vo.obj.impl.TcomsEquipmentImpl;
import com.mastars.vo.obj.impl.TirksCableEquipmentImpl;
import com.mastars.vo.obj.impl.TirksDXCEquipmentImpl;
import com.mastars.vo.obj.impl.TirksSCIDEquipmentImpl;
import com.mastars.vo.obj.impl.ULHEquipmentImpl;
import com.mastars.vo.obj.impl.UunetEquipmentImpl;
import com.mastars.vo.obj.impl.VEDSEquipmentImpl;
import com.mastars.vo.obj.impl.XNGEquipmentImpl;
import com.mcit.mastars2k.ConflictManagement.rule.BaseRules;
import com.mastars.vo.obj.impl.CircuitImpl;
import com.mastars.vo.obj.impl.EventImpl;
import com.mastars.vo.obj.impl.SwitchEquipmentImpl;
import com.mcit.mastars2k.ConflictManagement.rule.Transport.EquipmentRule;
import com.mcit.mastars2k.M2KDatabase.DbOnOffSwitch;
import com.mcit.mastars2k.M2KDatabase.MastarsDatabase;
import com.mcit.mastars2k.M2KUtils.CommonConstant;
import com.mcit.mastars2k.obj.Network;
import com.mcit.mastars2k.obj.impl.RequestImpl;
import com.mcit.mastars2k.obj.managers.RemoteDatabaseManager;
import com.mcit.mastars2k.utils.database.DatabaseConnection;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;
import com.vzbi.mastars.restclients.kirke.KirkeConflictService;
import com.vzbi.mastars.restclients.kirke.vo.ItemConflictDetails;
import com.vzbi.mastars.restclients.kirke.vo.ItemConflictEquipDetails;
import com.vzbi.persistent.process.CustomerDBReadProcess;

import junit.framework.Assert;

@RunWith(PowerMockRunner.class)
@PrepareForTest({MastarsDataBaseAccessFactory.class, MastarsClientFactory.class, EquipmentRule.class, CustomerDBReadProcess.class})
@PowerMockIgnore("javax.management.*")
public class EquipmentRuleTest {
	
	private MastarsDatabase db;
	private EquipmentRule equipmentRule;
	private DataBaseAccess dba;
	private DatabaseConnection dc;
	private ResultSet rs;
	private RemoteDatabaseManager rdm;
	private CustomerDBReadProcess custDB;
	
//	@Rule
//	public PowerMockRule rule = new PowerMockRule();
//	
//	static{
//		PowerMockAgent.initializeIfNeeded();
//	}
	
	@Before
	public void prepare() throws Exception {
		PowerMockito.mockStatic(MastarsDataBaseAccessFactory.class);
		PowerMockito.mockStatic(MastarsClientFactory.class);
		rdm = mock(RemoteDatabaseManager.class);
		when(MastarsClientFactory.getMastarsServer(RemoteDatabaseManager.class)).thenReturn(rdm);
		dba = mock(DataBaseAccess.class);
		when(MastarsDataBaseAccessFactory.getMastarsDataAccess(any(),any())).thenReturn(dba);
		rs = mock(ResultSet.class);
		dc = mock(DatabaseConnection.class);
		when(dc.getResultSet()).thenReturn(rs);
		when(dba.getConnection()).thenReturn(dc);
		Connection mockConn=mock(Connection.class);
	       PreparedStatement mockPreparedStmnt = mock(PreparedStatement.class);
			when(dc.getConnection()).thenReturn(mockConn);
			when(mockConn.prepareStatement(anyString())).thenReturn(mockPreparedStmnt);
			
			doNothing().when(mockPreparedStmnt).setString(anyInt(), anyString());
			when(mockPreparedStmnt.executeUpdate()).thenReturn(1);
			when(mockPreparedStmnt.executeQuery()).thenReturn(rs);
			when(mockPreparedStmnt.execute()).thenReturn(Boolean.TRUE);
			doNothing().when(mockConn).commit();
		custDB = mock(CustomerDBReadProcess.class);
		PowerMockito.whenNew(CustomerDBReadProcess.class).withNoArguments().thenReturn(custDB);
		equipmentRule = new EquipmentRule(new RequestImpl(), 0);
		db = new MastarsDatabase();
	}
	
	
	@Test
	public void testgetExistingItemIDs(){
		
		boolean hasException = false;
		
		try {
			RequestImpl request = mock(RequestImpl.class);
			Network network = mock(Network.class);
			EventImpl event = mock(EventImpl.class);
			CircuitImpl item = mock(CircuitImpl.class);
			List<Item> dummyItemList = new ArrayList<Item>();
			dummyItemList.add(item);
			setPrivateField(EquipmentRule.class.getSuperclass().getSuperclass().getDeclaredField("db"), this.db, equipmentRule);
			setPrivateField(EquipmentRule.class.getSuperclass().getSuperclass().getDeclaredField("event"), event, equipmentRule);
			setPrivateField(EquipmentRule.class.getSuperclass().getSuperclass().getDeclaredField("request"), request, equipmentRule);
			DbOnOffSwitch dbOnOffSwitch = mock(DbOnOffSwitch.class);
			setPrivateField(MastarsDatabase.class.getDeclaredField("dbOnOffSwitch"), dbOnOffSwitch, this.db);
			
			when(dbOnOffSwitch.isPropertyEnabled("CHECK_EQUIP_USPL_CONFLICT")).thenReturn(true);
			when(dbOnOffSwitch.isPropertyEnabled(CommonConstant.NGMETRO_CONFLICT_ENABLED)).thenReturn(true);
			when(rs.next()).thenReturn(true).thenReturn(false);
			when(rs.getLong("ITEM_ID")).thenReturn(12345L);
			when(event.getItemsAndSubItemsList()).thenReturn(dummyItemList);
			when(item.getItemId()).thenReturn(298347L);
			when(item.getType()).thenReturn(Item.CIRCUIT);
			when(item.getImpactType()).thenReturn(' ');
			when(rdm.getTransmissionIdByFneCircuitId(any())).thenReturn(new ArrayList<String>());
			when(item.getEquipmentAttributeByType(Item.CIRCUIT_TYPE)).thenReturn("R");
			when(custDB.getImpactedItemsList(Mockito.anyLong(), any(), any())).thenReturn(new ArrayList<Long>());
			when(network.getNetwork()).thenReturn("IEN");
			when(request.getNetwork()).thenReturn(network);
			when(request.getInvokingSystem()).thenReturn("");
			
			
			Method method = EquipmentRule.class.getDeclaredMethod("getExistingItemIDs", char.class, Item.class, List.class);
			method.setAccessible(true);
			
			equipmentRule.setIENConflictEnabled(true);
			method.invoke( equipmentRule, Item.CIRCUIT, item, new ArrayList<Long>() );
	//		
			equipmentRule.setIENConflictEnabled(false);
			method.invoke( equipmentRule, Item.CIRCUIT, item, new ArrayList<Long>() );
			
			
			SwitchEquipmentImpl switchItem = mock(SwitchEquipmentImpl.class);
			when(switchItem.getEquipType()).thenReturn("NGROADM");
			when(switchItem.getType()).thenReturn('S');
			when(switchItem.getEquipmentAttributeByType( Item.NODE )).thenReturn("node");
			
			equipmentRule.setIENConflictEnabled(true);
			method.invoke( equipmentRule, Item.SWITCH_EQUIPMENT, switchItem, new ArrayList<Long>() );
			
			equipmentRule.setIENConflictEnabled(false);
			method.invoke( equipmentRule, Item.SWITCH_EQUIPMENT, switchItem, new ArrayList<Long>() );
			
		} catch (Exception e) {
			e.printStackTrace();
			hasException = true;
		}
		
		Assert.assertFalse(hasException);
		
	}
	
	
	public void setPrivateField(Field field, Object value, Object forObject) throws IllegalArgumentException, IllegalAccessException {
		field.setAccessible(true);
		field.set(forObject, value);
	}
	
	
	private void getNetworkElementInfo(ItemImpl ii,char type){
Item it=mock(ItemImpl.class);
when(it.getType()).thenReturn(type);
it=(Item)ii;
String result=equipmentRule.getNetworkElementInfo(it);
Assert.assertNotNull(result);
Assert.assertEquals("Test", result);
}



private void getNetworkElementInfoForICON(ItemImpl ii,char type){
Item it=mock(ItemImpl.class);
when(it.getType()).thenReturn(type);
it=(Item)ii;
String result=equipmentRule.getNetworkElementInfo(it);
Assert.assertNotNull(result);
Assert.assertEquals("TEST|TEST", result);
}

private void getNetworkElementInfoForTCOMS(ItemImpl ii,char type){
Item it=mock(ItemImpl.class);
when(it.getType()).thenReturn(type);
it=(Item)ii;
String result=equipmentRule.getNetworkElementInfo(it);
Assert.assertNotNull(result);
Assert.assertEquals("TEST|TEST|TEST", result);
}

@Test
public void conflictFireRuleTest() throws Exception{

BaseRules rule =new EquipmentRule(new RequestImpl(), 0);
RequestImpl request = mock(RequestImpl.class);
EventImpl event = mock(EventImpl.class);
CircuitImpl item = mock(CircuitImpl.class);	
item.setItemId(1l);
List<Item> dummyItemList = new ArrayList<Item>();
dummyItemList.add(item);

List<Event> eventList =new ArrayList<>();				
event.setEventId(1l);
eventList.add(event);

when(request.getEventList()).thenReturn(eventList);
when(request.getRequestId()).thenReturn(0l);
when(event.getItemList()).thenReturn(dummyItemList);
when(item.getType()).thenReturn('C');
when(item.getCircuitId()).thenReturn("Test");
ItemConflictDetails[] itemConflictDtls=new ItemConflictDetails[1];
ItemConflictDetails itemConflict=new ItemConflictDetails();
ItemConflictEquipDetails ice=new ItemConflictEquipDetails();
List<ItemConflictEquipDetails> iceList=new ArrayList<>();
ice.setDevice("Test");
iceList.add(ice);
itemConflict.setRequestId(0l);
itemConflict.setItemDetails(iceList);
itemConflictDtls[0]=itemConflict;

	
KirkeConflictService kirkeConflictServ=PowerMockito.mock(KirkeConflictService.class);
Mockito.doReturn(itemConflictDtls).when(kirkeConflictServ).getConflictDtlsByNetElement("startDate","stopdate",new String[1]);

when(item.getItemId()).thenReturn(1l);
equipmentRule.conflictWithKirke(request,rule);
}

@Test
public void conflictWithKirkeTest() throws Exception{

HashMap<Long, Long> itemIdMap = new HashMap<>();
HashMap<String, Long> elementIdMap = new HashMap<>();
itemIdMap.put(1l, 1l);
elementIdMap.put("Test", 1l);

BaseRules rule =new EquipmentRule(new RequestImpl(), 0);
ItemConflictDetails[] itemConflictDtls=new ItemConflictDetails[1];
ItemConflictDetails itemConflict=new ItemConflictDetails();
ItemConflictEquipDetails ice=new ItemConflictEquipDetails();
List<ItemConflictEquipDetails> iceList=new ArrayList<>();
ice.setDevice("Test");
ice.setItemId(1l);
iceList.add(ice);
itemConflict.setRequestId(0l);
itemConflict.setItemDetails(iceList);
itemConflictDtls[0]=itemConflict;
equipmentRule.conflictFireRule(itemConflictDtls,elementIdMap, itemIdMap, rule);				
}

@Test
public void getNetworkElementInfoTest() throws Exception {

IconEquipmentImpl iconImpl = new IconEquipmentImpl();
iconImpl.setTidId("TEST");
iconImpl.setOpAmpChain("TEST");
getNetworkElementInfoForICON((ItemImpl) iconImpl,'R');
iconImpl.setTidId("");
iconImpl.setCableId("TEST");
iconImpl.setFiberNumber("TEST");
getNetworkElementInfoForICON((ItemImpl) iconImpl,'R');

VEDSEquipmentImpl vedsEquipment = new VEDSEquipmentImpl('g');
	vedsEquipment.setEquipmentName("Test");
	getNetworkElementInfo((ItemImpl) vedsEquipment,'g');
	
	     	
TcomsEquipmentImpl tci = new TcomsEquipmentImpl();
tci.setCircuitId("TEST");
tci.setTid("TEST");
tci.setAid("TEST");
getNetworkElementInfoForTCOMS((ItemImpl) tci,'T');

CircuitImpl ci =new CircuitImpl();
ci.setCircuitId("Test");
getNetworkElementInfo((ItemImpl) ci,'C');

DataEquipmentImpl di = new DataEquipmentImpl();
di.setNode("Test");
getNetworkElementInfo((ItemImpl) di,'D');

DefaultEquipmentImpl de = new DefaultEquipmentImpl();
de.setNode("Test");
getNetworkElementInfo((ItemImpl) de,'$');

DDCEquipmentImpl ddc = new DDCEquipmentImpl('j');
	ddc.setDevice("Test");
	getNetworkElementInfo((ItemImpl) ddc,'j');
	
	DxcEquipmentImpl dxcImpl = new DxcEquipmentImpl();
dxcImpl.setDxcId("Test");
getNetworkElementInfo((ItemImpl) dxcImpl,'Z');

EtsEquipmentImpl etsEquipment = new EtsEquipmentImpl();
	etsEquipment.setNode("Test");
	getNetworkElementInfo((ItemImpl) etsEquipment,'o');
	
	FIOSEquipmentImpl fiosImpl =new FIOSEquipmentImpl();
	fiosImpl.setEquipmentName("Test");
	getNetworkElementInfo((ItemImpl) fiosImpl,'f');

	
	IsnServerItemImpl isnServerItemImpl = new IsnServerItemImpl();
	isnServerItemImpl.setIsnServerName("Test");
	getNetworkElementInfo((ItemImpl) isnServerItemImpl,'i');
	
	Item item = PowerMockito.spy(new ItemImpl());        
when(item.getEquipmentType()).thenReturn("Test");
getNetworkElementInfo((ItemImpl) item,'E');

 item = PowerMockito.spy(new ItemImpl());        
when(item.getEquipmentType()).thenReturn("Test");
getNetworkElementInfo((ItemImpl) item,'G');

MeccaEquipmentImpl me = new MeccaEquipmentImpl();
when(me.getPointSpec()).thenReturn("Test");
getNetworkElementInfo((ItemImpl) item,'M');

XNGEquipmentImpl xngImpl = new XNGEquipmentImpl() ;
	xngImpl.setDevice("Test");
	getNetworkElementInfo((ItemImpl) xngImpl,'a'); 

xngImpl = new XNGEquipmentImpl() ;
	xngImpl.setDevice("Test");
	getNetworkElementInfo((ItemImpl) xngImpl,'b');
	
	SwitchEquipmentImpl switchEquipment = new SwitchEquipmentImpl();
switchEquipment.setNode("Test");
getNetworkElementInfo((ItemImpl) switchEquipment,'S');

TirksCableEquipment tirksCableEquipment = new TirksCableEquipmentImpl();
	tirksCableEquipment.setCableNumber("Test");
	getNetworkElementInfo((ItemImpl) tirksCableEquipment,'t');
	
	TirksDXCEquipment tirksDxcEquipment = new TirksDXCEquipmentImpl();
	tirksDxcEquipment.setDxcId("Test");
	getNetworkElementInfo((ItemImpl) tirksDxcEquipment,'r');
	
	TirksSCIDEquipment tirksSCIDEquipment = new TirksSCIDEquipmentImpl();
	tirksSCIDEquipment.setScid("Test");
	getNetworkElementInfo((ItemImpl) tirksSCIDEquipment,'s');
	
	
	ULHEquipmentImpl ulh= new ULHEquipmentImpl();
	ulh.setSystem("Test");
	getNetworkElementInfo((ItemImpl) ulh,'v');
	
	
	FiberCableEquipmentImpl cableImpl = new FiberCableEquipmentImpl();
	cableImpl.setCableID("Test");
	getNetworkElementInfo((ItemImpl) cableImpl,'^');
	
	UunetEquipmentImpl uunet_impl = new UunetEquipmentImpl();
uunet_impl.setDevice("Test");
getNetworkElementInfo((ItemImpl) uunet_impl,'V');
}
	
	@Test
	public void testgetExistingPhysicalTIDAgainstPhysicalTIDMaintItems() throws Exception {
		//prepare();
		
		//List<Long> dummyItemIds = new ArrayList<Long>();
		//dummyItemIds.add(123456L);
		//dummyItemIds.add(789012L);
		
		//Item item = new ItemImpl();
		//item.setItemId(123456l);
		//item.setEquipmentAttributeByType(Item.PHYS_TID, "");
		//item.setEquipmentAttributeByType(Item.NODE, "");
		
		//final AtomicInteger idx = new AtomicInteger(0);
		//doAnswer(new Answer<Boolean>() {
        //    @Override
        //    public Boolean answer(InvocationOnMock invocation) throws Throwable {
         //       int index = idx.getAndIncrement();
          //      if (dummyItemIds.size() > index) {
           //     	when(rs.getLong("ITEM_ID")).thenReturn(dummyItemIds.get(index));
//return true;
           //     } else
           //         return false;
           // }
        //}).when(rs).next();
		
		//PowerMockito.doReturn("").when(equipmentRule,"getOverlappingDateTimeSQL");
		
//		List<Long> itemIds = Whitebox.invokeMethod(equipmentRule,"getExistingPhysicalTIDAgainstPhysicalTIDMaintItems", item);
		//Method  method2 = EquipmentRule.class.getDeclaredMethod("getExistingPhysicalTIDAgainstPhysicalTIDMaintItems", Item.class);
		//method2.setAccessible(true);
		//List<Long> itemIds = (List<Long>) method2.invoke(equipmentRule,item);
		
		//Assert.assertNotNull(itemIds);
		//Assert.assertThat(itemIds, is(dummyItemIds));
	}
	
//	@Test
//	public void testgetExistingItemIDsForMecca() throws Exception {
//
//	char cType = 'M';
//	//mastarsDatabase=new MastarsDatabase();
//	MastarsDatabase mastarsDB=EasyMock.createMock(MastarsDatabase.class); 
//	PowerMockito.doReturn(mockedList()).when(equipmentRule, "getExistingItemIDs", Mockito.anyChar(), Mockito.anyObject(), Mockito.anyObject());
//	EasyMock.expect(mastarsDB.getItemIds(Mockito.anyString())).andReturn(mockedItemList());
//	Set<Long> itemIds = Whitebox.invokeMethod(equipmentRule,"getExistingItemIDs", Item.CIRCUIT, mockCircuitObject(), Mockito.anyObject());
//	Assert.assertNotNull(itemIds);
//	Assert.assertEquals(2, itemIds.size());
//	}
//	
//	@Test
//	public void testgetExistingItemIDsForFiberPanel() throws Exception {
//		
//		char cType = '^';
//		Item item = new FiberCableEquipmentImpl();
//		List<Long> uspItems = new ArrayList<Long>();
//		
//		Method  method2 = EquipmentRule.class.getDeclaredMethod("getExistingItemIDs", char.class, Item.class, List.class);
//		method2.setAccessible(true);
//		Set<Long> conflicts = (Set<Long>) method2.invoke(equipmentRule,cType,item,uspItems);
//		Assert.assertNotNull(conflicts);
//		Assert.assertEquals(2, conflicts.size());
//	}
//
//
//	
//	@Test
//	public void testgetExistingItemIDsForULH() throws Exception {
//		
//		char cType = 'v';
//		Item item = new ULHEquipmentImpl();
//		List<Long> uspItems = new ArrayList<Long>();
//		
//		Method  method2 = EquipmentRule.class.getDeclaredMethod("getExistingItemIDs", char.class, Item.class, List.class);
//		method2.setAccessible(true);
//		Set<Long> conflicts = (Set<Long>) method2.invoke(equipmentRule,cType,item,uspItems);
//		Assert.assertNotNull(conflicts);
//		Assert.assertEquals(2, conflicts.size());
//	}
//	
//	@Test
//	public void testgetExistingItemIDsForIarms() throws Exception {
//		
//		char cType = 'o';
//		Item item = new ULHEquipmentImpl();
//		List<Long> uspItems = new ArrayList<Long>();
//		
//		Method  method2 = EquipmentRule.class.getDeclaredMethod("getExistingItemIDs", char.class, Item.class, List.class);
//		method2.setAccessible(true);
//		Set<Long> conflicts = (Set<Long>) method2.invoke(equipmentRule,cType,item,uspItems);
//		Assert.assertNotNull(conflicts);
//		Assert.assertEquals(2, conflicts.size());
//	}
//	
//	private Set<Long> mockedList() {
//		Set<Long> tempList = new HashSet<>();
//		tempList.add(1L);
//		tempList.add(2L);
//		return tempList;
//		}
//
//		private List<Long> mockedItemList() {
//		List<Long> tempList = new ArrayList<Long>();
//		tempList.add(123456L);
//		tempList.add(456789L);
//		return tempList;
//		}
//
//		private Item mockCircuitObject(){
//		CircuitImpl item=new CircuitImpl();
//		item.setCircuitType(Item.CIRCUIT_TYPE_ILEC);
//		item.setCircuitId("TESTCIRCUIT");
//		return item;
//		}
			
}
