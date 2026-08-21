package com.ibm.di.cvt71.tp.implementaion;

import static com.ibm.di.tp.server.handler.UnitAndFuncSharedTests.verify_Error_Document_Is_Returned_For_Missing_TP_Role;
import static com.ibm.di.tp.server.handler.UnitAndFuncSharedTests.verify_Initiator_TP_Instance_State_After_Exhausting_Data_Source;
import static com.ibm.di.tp.server.handler.UnitAndFuncSharedTests.verify_Initiator_TP_Instance_State_While_Sending_Data;
import static com.ibm.di.tp.server.handler.UnitAndFuncSharedTests.verify_Initiator_TP_Instance_Survives_Restart_Of_The_TP_Server;
import static com.ibm.di.tp.server.handler.UnitAndFuncSharedTests.verify_Intermediary_TP_Instance_State_If_Enabled;
import static com.ibm.di.tp.server.handler.UnitAndFuncSharedTests.verify_Intermediary_TP_Instance_Survives_Restart_Of_The_TP_Server;
import static com.ibm.di.tp.server.handler.UnitAndFuncSharedTests.verify_Property_Sheet_Definition_Exists;
import static com.ibm.di.tp.server.handler.UnitAndFuncSharedTests.verify_Provider_TP_Instance_State_While_Waiting_For_Request;
import static com.ibm.di.tp.server.handler.UnitAndFuncSharedTests.verify_Provider_TP_Instance_Survives_Restart_Of_The_TP_Server;
import static com.ibm.di.tp.server.handler.UnitAndFuncSharedTests.verify_Service_Document_Has_Link_To_TP_Node_Feed;
import static com.ibm.di.tp.server.handler.UnitAndFuncSharedTests.verify_TP_Destination_Entry_Can_Be_Deleted;
import static com.ibm.di.tp.server.handler.UnitAndFuncSharedTests.verify_TP_Destination_Entry_Can_Be_Updated;
import static com.ibm.di.tp.server.handler.UnitAndFuncSharedTests.verify_TP_Inst_Entry_Can_Be_Deleted;
import static com.ibm.di.tp.server.handler.UnitAndFuncSharedTests.verify_TP_Inst_Entry_Is_Accessible_From_TP_Inst_Feed_After_It_Is_Created;
import static com.ibm.di.tp.server.handler.UnitAndFuncSharedTests.verify_TP_Inst_Entry_Is_Correct;
import static com.ibm.di.tp.server.handler.UnitAndFuncSharedTests.verify_TP_Inst_Feed_Contains_Required_Categories_And_Has_No_Entries;
import static com.ibm.di.tp.server.handler.UnitAndFuncSharedTests.verify_TP_Instance_Destinations_Survives_Restart_Of_The_TP_Server;
import static com.ibm.di.tp.server.handler.UnitAndFuncSharedTests.verify_TP_Instance_Entry_Can_Be_Updated;
import static com.ibm.di.tp.server.handler.UnitAndFuncSharedTests.verify_TP_Instance_State_When_Disabled;
import static com.ibm.di.tp.server.handler.UnitAndFuncSharedTests.verify_TP_Node_Entry_Contains_The_Configured_Data_And_Link_To_TP_Type_Feed;
import static com.ibm.di.tp.server.handler.UnitAndFuncSharedTests.verify_TP_Node_Feed_Contains_Expected_Categories;
import static com.ibm.di.tp.server.handler.UnitAndFuncSharedTests.verify_TP_Node_Feed_Contains_Links_To_TP_Node_Entries;
import static com.ibm.di.tp.server.handler.UnitAndFuncSharedTests.verify_TP_Type_Entry_Contains_Required_Categories_And_A_Link_To_TP_Inst_Feed;
import static com.ibm.di.tp.server.handler.UnitAndFuncSharedTests.verify_TP_Type_Feed_Contains_Links_To_TP_Type_Entries;
import static com.ibm.di.tp.server.handler.UnitAndFuncSharedTests.verify_The_Newly_Created_Initiator_TP_Instance_Is_Started_On_The_TDI_Server;
import static com.ibm.di.tp.server.handler.UnitAndFuncSharedTests.verify_The_Newly_Created_Intermediary_TP_Instance_Is_Started_On_The_TDI_Server;
import static com.ibm.di.tp.server.handler.UnitAndFuncSharedTests.verify_The_Newly_Created_Provider_TP_Instance_Is_Started_On_The_TDI_Server;

import com.ibm.di.web.common.atom.AtomEntry;
import org.junit.Test;

import com.ibm.di.test.CVTComponent;
import com.ibm.di.test.CVTTest;
import com.ibm.di.test.tp.FuncTestTPClientContext;
import com.ibm.di.test.tp.TpAppHelper;
import com.ibm.di.tp.server.config.node.TdiNodeConfig;
import com.ibm.di.tp.server.model.TouchpointRole;

/**
 * 
 * <br>
 * <br>
 * <b>Note:</b> getClientContext() class is for internal usage only. Any
 * dependency from the end-user will not be supported. Changes to
 * getClientContext() class will happen without a warning.
 * 
 * @since 7.1
 */
@CVTComponent(name = "tpserver")
public class FN_39_TP_Server_Implementation_CVT extends FuncTestTPClientContext {

	private TpAppHelper app = new TpAppHelper(FuncTestTPClientContext.getClientContext());

	/**
	 * Copyright.
	 */
	@SuppressWarnings("unused")
	private static final String COPYRIGHT = com.ibm.di.server.CopyRight.OBJECT_CODE;

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC01")
	public void test_TC01() throws Exception {
		verify_Service_Document_Has_Link_To_TP_Node_Feed(getClientContext());
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC02")
	public void test_TC02() throws Exception {
		verify_TP_Node_Feed_Contains_Expected_Categories(getClientContext());
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC03")
	public void test_TC03() throws Exception {
		verify_TP_Node_Feed_Contains_Links_To_TP_Node_Entries(getClientContext());
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC04")
	public void test_TC04() throws Exception {
		TdiNodeConfig remoteNode = null;

		for (TdiNodeConfig node : getTPServerConfig().getNodeConfigs().getTdiNodeConfigs()) {
			if (ID_NODE_REMOTE.equals(node.getId())) {
				remoteNode = node;
				break;
			}
		}

		verify_TP_Node_Entry_Contains_The_Configured_Data_And_Link_To_TP_Type_Feed(getClientContext(), ID_NODE_REMOTE, remoteNode
				.getTitle(), remoteNode.getAuthor(), remoteNode.getEmail(), remoteNode.getSummary(), remoteNode.getContact(),
				remoteNode.getLocation(), remoteNode.getOrganization());
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC05")
	public void test_TC05() throws Exception {
		TdiNodeConfig localNode = null;

		for (TdiNodeConfig node : getTPServerConfig().getNodeConfigs().getTdiNodeConfigs()) {
			if (ID_NODE_LOCAL.equals(node.getId())) {
				localNode = node;
				break;
			}
		}

		verify_TP_Node_Entry_Contains_The_Configured_Data_And_Link_To_TP_Type_Feed(getClientContext(), ID_NODE_LOCAL, localNode
				.getTitle(), localNode.getAuthor(), localNode.getEmail(), localNode.getSummary(), localNode.getContact(), localNode
				.getLocation(), localNode.getOrganization());
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC06")
	public void test_TC06() throws Exception {
		verify_TP_Type_Feed_Contains_Links_To_TP_Type_Entries(getClientContext(), ID_NODE_LOCAL);
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC07")
	public void test_TC07() throws Exception {
		verify_TP_Type_Entry_Contains_Required_Categories_And_A_Link_To_TP_Inst_Feed(getClientContext(), ID_NODE_LOCAL,
				ID_TYPE_CONNECTOR);
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC08")
	public void test_TC08() throws Exception {
		verify_TP_Type_Entry_Contains_Required_Categories_And_A_Link_To_TP_Inst_Feed(getClientContext(), ID_NODE_LOCAL,
				ID_TYPE_CUSTOM);
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC09")
	public void test_TC09() throws Exception {
		verify_TP_Inst_Feed_Contains_Required_Categories_And_Has_No_Entries(getClientContext(), ID_NODE_LOCAL, ID_TYPE_CONNECTOR);
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC10")
	public void test_TC10() throws Exception {
		verify_TP_Inst_Feed_Contains_Required_Categories_And_Has_No_Entries(getClientContext(), ID_NODE_LOCAL, ID_TYPE_CUSTOM);
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC11")
	public void test_TC11() throws Exception {
		AtomEntry instEntry = null;
		try {
			instEntry = verify_TP_Inst_Entry_Is_Accessible_From_TP_Inst_Feed_After_It_Is_Created(getClientContext(), ID_NODE_LOCAL,
					ID_TYPE_CONNECTOR, TouchpointRole.PROVIDER);
		} finally {
			// cleanup
			if (instEntry != null) {
				app.deleteInstEntry(instEntry);
			}
		}
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC12")
	public void test_TC12() throws Exception {
		AtomEntry instEntry = null;
		try {
			instEntry = verify_TP_Inst_Entry_Is_Accessible_From_TP_Inst_Feed_After_It_Is_Created(getClientContext(), ID_NODE_LOCAL,
					ID_TYPE_CONNECTOR, TouchpointRole.INITIATOR);
		} finally {
			// cleanup
			if (instEntry != null) {
				app.deleteInstEntry(instEntry);
			}
		}
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC13")
	public void test_TC13() throws Exception {
		AtomEntry instEntry = null;
		try {
			instEntry = verify_TP_Inst_Entry_Is_Accessible_From_TP_Inst_Feed_After_It_Is_Created(getClientContext(), ID_NODE_LOCAL,
					ID_TYPE_CUSTOM, TouchpointRole.PROVIDER);
		} finally {
			// cleanup
			if (instEntry != null) {
				app.deleteInstEntry(instEntry);
			}
		}
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC14")
	public void test_TC14() throws Exception {
		AtomEntry instEntry = null;
		try {
			instEntry = verify_TP_Inst_Entry_Is_Accessible_From_TP_Inst_Feed_After_It_Is_Created(getClientContext(), ID_NODE_LOCAL,
					ID_TYPE_CUSTOM, TouchpointRole.INITIATOR);
		} finally {
			// cleanup
			if (instEntry != null) {
				app.deleteInstEntry(instEntry);
			}
		}
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC15")
	public void test_TC15() throws Exception {
		AtomEntry instEntry = null;
		try {
			instEntry = verify_TP_Inst_Entry_Is_Correct(getClientContext(), ID_NODE_LOCAL, ID_TYPE_CONNECTOR,
					TouchpointRole.PROVIDER);
		} finally {
			// cleanup
			if (instEntry != null) {
				app.deleteInstEntry(instEntry);
			}
		}
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC16")
	public void test_TC16() throws Exception {
		AtomEntry instEntry = null;
		try {
			instEntry = verify_TP_Inst_Entry_Is_Correct(getClientContext(), ID_NODE_LOCAL, ID_TYPE_CONNECTOR,
					TouchpointRole.INITIATOR);
		} finally {
			// cleanup
			if (instEntry != null) {
				app.deleteInstEntry(instEntry);
			}
		}
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC17")
	public void test_TC17() throws Exception {
		AtomEntry instEntry = null;
		try {
			instEntry = verify_TP_Inst_Entry_Is_Correct(getClientContext(), ID_NODE_LOCAL, ID_TYPE_CUSTOM, TouchpointRole.PROVIDER);
		} finally {
			// cleanup
			if (instEntry != null) {
				app.deleteInstEntry(instEntry);
			}
		}
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC18")
	public void test_TC18() throws Exception {
		AtomEntry instEntry = null;
		try {
			instEntry = verify_TP_Inst_Entry_Is_Correct(getClientContext(), ID_NODE_LOCAL, ID_TYPE_CUSTOM, TouchpointRole.INITIATOR);
		} finally {
			// cleanup
			if (instEntry != null) {
				app.deleteInstEntry(instEntry);
			}
		}
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC19")
	public void test_TC19() throws Exception {
		verify_TP_Inst_Entry_Can_Be_Deleted(getClientContext(), ID_NODE_LOCAL, ID_TYPE_CONNECTOR, TouchpointRole.PROVIDER);
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC20")
	public void test_TC20() throws Exception {
		verify_TP_Inst_Entry_Can_Be_Deleted(getClientContext(), ID_NODE_LOCAL, ID_TYPE_CONNECTOR, TouchpointRole.INITIATOR);
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC21")
	public void test_TC21() throws Exception {
		verify_TP_Inst_Entry_Can_Be_Deleted(getClientContext(), ID_NODE_LOCAL, ID_TYPE_CUSTOM, TouchpointRole.PROVIDER);
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC22")
	public void test_TC22() throws Exception {
		verify_TP_Inst_Entry_Can_Be_Deleted(getClientContext(), ID_NODE_LOCAL, ID_TYPE_CUSTOM, TouchpointRole.INITIATOR);
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC39")
	public void test_TC39() throws Exception {
		verify_The_Newly_Created_Provider_TP_Instance_Is_Started_On_The_TDI_Server(getClientContext(), ID_NODE_LOCAL,
				ID_TYPE_CONNECTOR);
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC40")
	public void test_TC40() throws Exception {
		verify_The_Newly_Created_Initiator_TP_Instance_Is_Started_On_The_TDI_Server(getClientContext(), ID_NODE_LOCAL,
				ID_TYPE_CONNECTOR);
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC41")
	public void test_TC41() throws Exception {
		verify_The_Newly_Created_Provider_TP_Instance_Is_Started_On_The_TDI_Server(getClientContext(), ID_NODE_LOCAL,
				ID_TYPE_CUSTOM);
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC42")
	public void test_TC42() throws Exception {
		verify_The_Newly_Created_Initiator_TP_Instance_Is_Started_On_The_TDI_Server(getClientContext(), ID_NODE_LOCAL,
				ID_TYPE_CUSTOM);
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC43")
	public void test_TC43() throws Exception {
		verify_Provider_TP_Instance_Survives_Restart_Of_The_TP_Server(getClientContext(), ID_NODE_LOCAL, ID_TYPE_CONNECTOR,
				getTDIServer());
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC44")
	public void test_TC44() throws Exception {
		verify_Initiator_TP_Instance_Survives_Restart_Of_The_TP_Server(getClientContext(), ID_NODE_LOCAL, ID_TYPE_CONNECTOR,
				getTDIServer());
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC45")
	public void test_TC45() throws Exception {
		verify_Provider_TP_Instance_Survives_Restart_Of_The_TP_Server(getClientContext(), ID_NODE_LOCAL, ID_TYPE_CUSTOM,
				getTDIServer());
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC46")
	public void test_TC46() throws Exception {
		verify_Initiator_TP_Instance_Survives_Restart_Of_The_TP_Server(getClientContext(), ID_NODE_LOCAL, ID_TYPE_CUSTOM,
				getTDIServer());
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC47")
	public void test_TC47() throws Exception {
		verify_Provider_TP_Instance_State_While_Waiting_For_Request(getClientContext(), ID_NODE_LOCAL, ID_TYPE_CONNECTOR);
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC48")
	public void test_TC48() throws Exception {
		verify_Initiator_TP_Instance_State_While_Sending_Data(getClientContext(), ID_NODE_LOCAL, ID_TYPE_CONNECTOR);
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC49")
	public void test_TC49() throws Exception {
		verify_Initiator_TP_Instance_State_After_Exhausting_Data_Source(getClientContext(), ID_NODE_LOCAL, ID_TYPE_CONNECTOR);
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC50")
	public void test_TC50() throws Exception {
		verify_TP_Instance_State_When_Disabled(getClientContext(), ID_NODE_LOCAL, ID_TYPE_CONNECTOR, TouchpointRole.PROVIDER);
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC51")
	public void test_TC51() throws Exception {
		verify_TP_Instance_State_When_Disabled(getClientContext(), ID_NODE_LOCAL, ID_TYPE_CONNECTOR, TouchpointRole.INITIATOR);
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC52")
	public void test_TC52() throws Exception {
		verify_Provider_TP_Instance_State_While_Waiting_For_Request(getClientContext(), ID_NODE_LOCAL, ID_TYPE_CUSTOM);
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC53")
	public void test_TC53() throws Exception {
		verify_Initiator_TP_Instance_State_While_Sending_Data(getClientContext(), ID_NODE_LOCAL, ID_TYPE_CUSTOM);
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC54")
	public void test_TC54() throws Exception {
		verify_Initiator_TP_Instance_State_After_Exhausting_Data_Source(getClientContext(), ID_NODE_LOCAL, ID_TYPE_CUSTOM);
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC55")
	public void test_TC55() throws Exception {
		verify_TP_Instance_State_When_Disabled(getClientContext(), ID_NODE_LOCAL, ID_TYPE_CUSTOM, TouchpointRole.PROVIDER);
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC56")
	public void test_TC56() throws Exception {
		verify_TP_Instance_State_When_Disabled(getClientContext(), ID_NODE_LOCAL, ID_TYPE_CUSTOM, TouchpointRole.INITIATOR);
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC57")
	public void test_TC57() throws Exception {
		verify_Property_Sheet_Definition_Exists(getClientContext(), ID_NODE_LOCAL, ID_TYPE_CONNECTOR);
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC58")
	public void test_TC58() throws Exception {
		verify_Property_Sheet_Definition_Exists(getClientContext(), ID_NODE_LOCAL, ID_TYPE_CUSTOM);
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC59")
	public void test_TC59() throws Exception {
		verify_TP_Instance_Entry_Can_Be_Updated(getClientContext(), ID_NODE_LOCAL, ID_TYPE_CONNECTOR);
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC60")
	public void test_TC60() throws Exception {
		verify_TP_Destination_Entry_Can_Be_Updated(getClientContext(), ID_NODE_LOCAL, ID_TYPE_CONNECTOR);
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC61")
	public void test_TC61() throws Exception {
		verify_TP_Destination_Entry_Can_Be_Deleted(getClientContext(), ID_NODE_LOCAL, ID_TYPE_CONNECTOR);
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC62")
	public void test_TC62() throws Exception {
		verify_Error_Document_Is_Returned_For_Missing_TP_Role(getClientContext(), ID_NODE_LOCAL, ID_TYPE_CONNECTOR);
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC63")
	public void test_TC63() throws Exception {
		verify_TP_Inst_Entry_Is_Accessible_From_TP_Inst_Feed_After_It_Is_Created(getClientContext(), ID_NODE_LOCAL,
				ID_TYPE_INTERMEDIARY, TouchpointRole.INTERMEDIARY);
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC64")
	public void test_TC64() throws Exception {
		verify_TP_Inst_Entry_Is_Accessible_From_TP_Inst_Feed_After_It_Is_Created(getClientContext(), ID_NODE_LOCAL, ID_TYPE_CUSTOM,
				TouchpointRole.INTERMEDIARY);
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC65")
	public void test_TC65() throws Exception {
		verify_TP_Inst_Entry_Is_Correct(getClientContext(), ID_NODE_LOCAL, ID_TYPE_INTERMEDIARY, TouchpointRole.INTERMEDIARY);
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC66")
	public void test_TC66() throws Exception {
		verify_TP_Inst_Entry_Is_Correct(getClientContext(), ID_NODE_LOCAL, ID_TYPE_CUSTOM, TouchpointRole.INTERMEDIARY);
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC67")
	public void test_TC67() throws Exception {
		verify_TP_Inst_Entry_Can_Be_Deleted(getClientContext(), ID_NODE_LOCAL, ID_TYPE_INTERMEDIARY, TouchpointRole.INTERMEDIARY);
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC68")
	public void test_TC68() throws Exception {
		verify_TP_Inst_Entry_Can_Be_Deleted(getClientContext(), ID_NODE_LOCAL, ID_TYPE_CUSTOM, TouchpointRole.INTERMEDIARY);
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC69")
	public void test_TC69() throws Exception {
		verify_TP_Instance_State_When_Disabled(getClientContext(), ID_NODE_LOCAL, ID_TYPE_INTERMEDIARY, TouchpointRole.INTERMEDIARY);
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC70")
	public void test_TC70() throws Exception {
		verify_TP_Instance_State_When_Disabled(getClientContext(), ID_NODE_LOCAL, ID_TYPE_CUSTOM, TouchpointRole.INTERMEDIARY);
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC71")
	public void test_TC71() throws Exception {
		verify_The_Newly_Created_Intermediary_TP_Instance_Is_Started_On_The_TDI_Server(getClientContext(), ID_NODE_LOCAL,
				ID_TYPE_INTERMEDIARY, ID_TYPE_CONNECTOR);
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC72")
	public void test_TC72() throws Exception {
		verify_The_Newly_Created_Intermediary_TP_Instance_Is_Started_On_The_TDI_Server(getClientContext(), ID_NODE_LOCAL,
				ID_TYPE_CUSTOM, ID_TYPE_CONNECTOR);
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC73")
	public void test_TC73() throws Exception {
		verify_Intermediary_TP_Instance_Survives_Restart_Of_The_TP_Server(getClientContext(), ID_NODE_LOCAL, ID_TYPE_INTERMEDIARY,
				getTDIServer(), ID_TYPE_CONNECTOR);
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC74")
	public void test_TC74() throws Exception {
		verify_Intermediary_TP_Instance_Survives_Restart_Of_The_TP_Server(getClientContext(), ID_NODE_LOCAL, ID_TYPE_CUSTOM,
				getTDIServer(), ID_TYPE_CONNECTOR);
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC75")
	public void test_TC75() throws Exception {
		verify_Intermediary_TP_Instance_State_If_Enabled(getClientContext(), ID_NODE_LOCAL, ID_TYPE_INTERMEDIARY, ID_TYPE_CONNECTOR);
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC76")
	public void test_TC76() throws Exception {
		verify_Intermediary_TP_Instance_State_If_Enabled(getClientContext(), ID_NODE_LOCAL, ID_TYPE_CUSTOM, ID_TYPE_CONNECTOR);
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC77")
	public void test_TC77() throws Exception {
		verify_TP_Instance_Destinations_Survives_Restart_Of_The_TP_Server(getClientContext(), ID_NODE_LOCAL, ID_TYPE_CONNECTOR,
				getTDIServer());
	}

	@Test
	@CVTTest(name = "CVT_FN-39_TP_Server_Implementation_TC78")
	public void test_TC78() throws Exception {
		verify_TP_Instance_Destinations_Survives_Restart_Of_The_TP_Server(getClientContext(), ID_NODE_LOCAL, ID_TYPE_CUSTOM,
				getTDIServer());
	}
}
