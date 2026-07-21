// Implements NTMetaData.h definitions.
// Implements JNI interface functions.

#ifndef UNICODE
#define UNICODE
#endif
#include <wchar.h>
#include <windows.h>
#include <lm.h>
#include <vector>
#include "Wrapper_LPWSTR.h"
#include "NTMetaDataException.h"
#include "NTMetaData.h"
#include "fstream"
#include "time.h"
#include "com_ibm_di_connector_NT4UserMetaDataConnector_NTMetaData.h"

using namespace std;


#define NTMetaDataException_JavaClassName "com/ibm/di/connector/NT4UserMetaDataConnector/NT4UserMetaDataException"

#define NET_BUF_SIZE 4096
#define MAX_ACCOUNT_NAME_LEN 4096
#define MAX_DOMAIN_NAME_LEN 4096

#define MEMBERS_TYPE_USERS			1
#define MEMBERS_TYPE_GROUPS			2
#define MEMBERS_TYPE_USERS_GROUPS	3

#ifdef DO_LOG
#define PRINT_TO_LOG(msg) PrintToLog(##msg); 
#else
#define PRINT_TO_LOG(msg)
#endif




// ****************************************************************
// Utility Log Routines
// ****************************************************************

Wrapper_LPWSTR DumpGroupInfo(GroupInfo &p_info)
{
	Wrapper_LPWSTR s = L"\n\tGroupInfo Dump:";
	s += L"\n\t\tName:\t\t";s += p_info.name;
	s += L"\n\t\tComment:\t";s += p_info.comment;
	s += L"\n\t\tIsGlobal:\t";s += p_info.is_global?L"true":L"false";
	return s;
}

Wrapper_LPWSTR DumpUserInfo(UserInfo &p_info)
{
	Wrapper_LPWSTR s = L"\n\tUserInfo Dump:";

	s += L"\n\t\tName\t\t\t\t:"; s += p_info.name;
    s += L"\n\t\tPassword\t\t\t:"; s += p_info.password;
    s += L"\n\t\tPassword_age\t\t:"; s += p_info.password_age;
    s += L"\n\t\tPriv\t\t\t\t:"; s += p_info.priv;
    s += L"\n\t\tHome_dir\t\t\t:"; s += p_info.home_dir;
    s += L"\n\t\tComment\t\t\t\t:"; s += p_info.comment;
    s += L"\n\t\tFlags\t\t\t\t:"; s += p_info.flags;
    s += L"\n\t\tScript_path\t\t\t:"; s += p_info.script_path;
    s += L"\n\t\tAuth_flags\t\t\t:"; s += p_info.auth_flags;
    s += L"\n\t\tFull_name\t\t\t:"; s += p_info.full_name;
    s += L"\n\t\tUsr_comment\t\t\t:"; s += p_info.usr_comment;
    s += L"\n\t\tParms\t\t\t\t:"; s += p_info.parms;
    s += L"\n\t\tWorkstations\t\t:"; s += p_info.workstations;
    s += L"\n\t\tLast_logon\t\t\t:"; s += p_info.last_logon;
    s += L"\n\t\tLast_logoff\t\t\t:"; s += p_info.last_logoff;
    s += L"\n\t\tAcct_expires\t\t:"; s += p_info.acct_expires;
    s += L"\n\t\tMax_storage\t\t\t:"; s += p_info.max_storage;
    s += L"\n\t\tUnits_per_week\t\t:"; s += p_info.units_per_week;
	s += L"\n\t\tLogon Hours\t\t\t: ";
	if(p_info.is_logon_hours_null)
	{
		s += L"NULL";
	}
	else
	{
		for (int i=0; i < LOGON_HOURS_ARRAY_LENGTH; i++)
		{
			s += L"( ";
			s += i;
			s += L" : ";
			s += p_info.logon_hours[i];
			s += L" )  ";
		}
	}
    s += L"\n\t\tBad_pw_count\t\t:"; s += p_info.bad_pw_count;
    s += L"\n\t\tNum_logons\t\t\t:"; s += p_info.num_logons;
    s += L"\n\t\tLogon_server\t\t:"; s += p_info.logon_server;
    s += L"\n\t\tCountry_code\t\t:"; s += p_info.country_code;
    s += L"\n\t\tCode_page\t\t\t:"; s += p_info.code_page;
    s += L"\n\t\tUser_id\t\t\t\t:"; s += p_info.user_id;
    s += L"\n\t\tPrimary_group_id\t:"; s += p_info.primary_group_id;
    s += L"\n\t\tProfile\t\t\t\t:"; s += p_info.profile;
    s += L"\n\t\tHome_dir_drive\t\t:"; s += p_info.home_dir_drive;
    s += L"\n\t\tPassword_expired\t:"; s += p_info.password_expired;
	s += L"\n\t\tPrimary_group_name\t:"; s += p_info.primary_group_name;

	return s;
}
	
void PrintToLog(Wrapper_LPWSTR msg)
{
	::ofstream log_file("NTMetaData.log", ios::app);

	time_t current_time;
	char time_str[100];
	time(&current_time);
	strftime(&time_str[0], 99, "%Y.%m.%d %H:%M:%S", localtime(&current_time));

	log_file << "\n\n\n" << time_str << "\n" << msg;
	log_file.close();
}






// ****************************************************************
// GroupInfo Implementation
// ****************************************************************


GroupInfo::GroupInfo(JNIEnv *p_env, const jobject p_info)
{
	JavaObject jo(p_env, p_info);
	
	name = jo.GetStringField("mGroupName");
	comment = jo.GetStringField("mComment");
	is_global = jo.GetBooleanObjectField("mIsGlobal");
}






// ****************************************************************
// UserInfo Implementation
// ****************************************************************


UserInfo::UserInfo(JNIEnv *p_env, const jobject p_info)
{
	
	JavaObject jo(p_env, p_info);

	name = jo.GetStringField("mUserName");

	comment = jo.GetStringField("mAccountComment");

	full_name = jo.GetStringField("mFullName");

	usr_comment = jo.GetStringField("mUserComment");

	password = jo.GetStringField("mPassword", is_password_null);

	// Convert Days to Seconds
	password_age = (int)jo.GetLongObjectField("mPasswordAge") * (60*60*24); 

	priv = jo.GetIntegerObjectField("mPrivilegeLevel");

	home_dir = jo.GetStringField("mHomeDirectory");

	flags = jo.GetIntegerObjectField("mFlags");

	script_path = jo.GetStringField("mScriptPath");

	auth_flags = jo.GetIntegerObjectField("mAuthFlags");

	parms = jo.GetStringField("mApplicationsParams");

	workstations = jo.GetStringField("mLogonWorkstations");

	// Convert Milliseconds to Seconds
	last_logon = (int)jo.GetDateField("mLastLogon")/1000; 

	// Convert Milliseconds to Seconds
	last_logoff = (int)jo.GetDateField("mLastLogoff")/1000; 

	// Convert Milliseconds to Seconds
	acct_expires = (int)jo.GetDateField("mAccountExpDate")/1000; 

	max_storage = (int)jo.GetLongObjectField("mMaxAccDiskSpace");

	units_per_week = jo.GetIntegerObjectField("mUnitsPerWeek");

	jo.GetByteArrayField("mLogonHours", logon_hours, LOGON_HOURS_ARRAY_LENGTH, is_logon_hours_null);

	bad_pw_count = jo.GetIntegerObjectField("mBadPasswordCnt");

	num_logons = jo.GetIntegerObjectField("mLogonsNum");

	logon_server = jo.GetStringField("mLogonServer");

	country_code = jo.GetIntegerObjectField("mCountryCode");

	code_page = jo.GetIntegerObjectField("mCodePage");

	user_id = jo.GetIntegerObjectField("mRelativeUserID");

	primary_group_id = jo.GetIntegerObjectField("mPrimaryGroupID");

	profile = jo.GetStringField("mProfilePath");

	home_dir_drive = jo.GetStringField("mHomeDirectoryDrive");

	password_expired = jo.GetIntegerObjectField("mPasswordExpired");

	primary_group_name = jo.GetStringField("mPrimaryGroup");
	
}



jobject GroupInfo::GetJObject(JNIEnv *p_env) const
{	
	JavaObject *tmp_obj = new JavaObject(p_env, "com/ibm/di/connector/NT4UserMetaDataConnector/GroupInfo");
	

	tmp_obj->SetStringField("mGroupName", name);
	tmp_obj->SetStringField("mComment", comment);
	tmp_obj->SetBooleanObjectField("mIsGlobal", is_global);
	
	return tmp_obj->GetJavaObject();
}



jobject UserInfo::GetJObject(JNIEnv *p_env) const
{	
	JavaObject *tmp_obj = new JavaObject(p_env, "com/ibm/di/connector/NT4UserMetaDataConnector/UserInfo");

	tmp_obj->SetStringField("mUserName", name);

	tmp_obj->SetStringField("mAccountComment", comment);

	tmp_obj->SetStringField("mFullName", full_name);

	tmp_obj->SetStringField("mUserComment", usr_comment);

	// Always return password as NULL
	tmp_obj->SetStringField("mPassword", NULL);  

	// Convert Seconds to Days
	tmp_obj->SetLongObjectField("mPasswordAge", password_age / (60*60*24)); 

	tmp_obj->SetIntegerObjectField("mPrivilegeLevel", priv);

	tmp_obj->SetStringField("mHomeDirectory", home_dir);

	tmp_obj->SetIntegerObjectField("mFlags", flags);

	tmp_obj->SetStringField("mScriptPath", script_path);

	tmp_obj->SetIntegerObjectField("mAuthFlags", auth_flags);

	tmp_obj->SetStringField("mApplicationsParams", parms);

	tmp_obj->SetStringField("mLogonWorkstations", workstations);

	// Convert Seconds to Milliseconds
	tmp_obj->SetDateField("mLastLogon", jlong(last_logon)*1000); 

	// Convert Seconds to Milliseconds
	tmp_obj->SetDateField("mLastLogoff", jlong(last_logoff)*1000); 

	tmp_obj->SetDateField("mAccountExpDate", jlong(acct_expires)*1000);

	tmp_obj->SetLongObjectField("mMaxAccDiskSpace", max_storage);

	tmp_obj->SetIntegerObjectField("mUnitsPerWeek", units_per_week);

	if(is_logon_hours_null)
	{
		tmp_obj->SetByteArrayField("mLogonHours", NULL, LOGON_HOURS_ARRAY_LENGTH);
	}
	else
	{
		tmp_obj->SetByteArrayField("mLogonHours", logon_hours, LOGON_HOURS_ARRAY_LENGTH);
	}

	tmp_obj->SetIntegerObjectField("mBadPasswordCnt", bad_pw_count);

	tmp_obj->SetIntegerObjectField("mLogonsNum", num_logons);

	tmp_obj->SetStringField("mLogonServer", logon_server);

	tmp_obj->SetIntegerObjectField("mCountryCode", country_code);

	tmp_obj->SetIntegerObjectField("mCodePage", code_page);

	tmp_obj->SetIntegerObjectField("mRelativeUserID", user_id);

	tmp_obj->SetIntegerObjectField("mPrimaryGroupID", primary_group_id);

	tmp_obj->SetStringField("mProfilePath", profile);

	tmp_obj->SetStringField("mHomeDirectoryDrive", home_dir_drive);

	tmp_obj->SetIntegerObjectField("mPasswordExpired", password_expired);

	tmp_obj->SetStringField("mPrimaryGroup", primary_group_name);

	return tmp_obj->GetJavaObject();
}




// ****************************************************************
// NTMetaData class implementation
// ****************************************************************


bool NTMetaData::LogOn(Wrapper_LPWSTR p_user_name, Wrapper_LPWSTR p_password) const
{
	NETRESOURCE nr;

	nr.dwType = RESOURCETYPE_ANY;
	nr.lpLocalName = NULL;
	nr.lpRemoteName = m_server_name;
	nr.lpProvider = NULL;
	

	DWORD err = WNetAddConnection2(&nr, p_password, p_user_name, 0);

	return err == NO_ERROR;
}



bool NTMetaData::IsPrimaryDomainController() const
{
	SERVER_INFO_101 *server_info = NULL;

	try 
	{
		NET_API_STATUS result = NetServerGetInfo(m_server_name, 101, (LPBYTE*) &server_info);
		if (result != NERR_Success)
			throw NTMetaDataException(L"NetServerGetInfo", result);
		
	} catch (NTMetaDataException)
	{
		NetApiBufferFree(server_info);
		throw;
	}

	bool is_pdc = (server_info->sv101_type & SV_TYPE_DOMAIN_CTRL)?true:false;
	NetApiBufferFree(server_info);

	return is_pdc;
}



const Wrapper_LPWSTR NTMetaData::GetPureUserName(Wrapper_LPWSTR name) const
{
	wstring pure_name = name;
	wstring s = name;
	wstring::size_type found_index = s.find(L"\\");
	if (found_index != wstring::npos)
	{
		Wrapper_LPWSTR name_prefix = SERVER_NAME_PREFIX + s.substr(0, found_index);

		// Check if name_prefix is a computer name
		DWORD returned_entry_count;
		PVOID buf = NULL;
		NET_API_STATUS result = NetQueryDisplayInformation(name_prefix, 2, 0, 1000, 4096, &returned_entry_count, &buf);

		// It's a computer name, removing it from the name of the user
		if (result == NERR_Success) 
		{	
			pure_name = s.substr(found_index+1);
		}
		NetApiBufferFree(buf);

	}
	return pure_name;
}



// ----------------------
// Global Group Functions
// ----------------------


// Retrieving Functions


const DWORD NTMetaData::GetGlobalGroupsNames(DWORD start_index, 
											 DWORD entries_requested,
											 vector<Wrapper_LPWSTR> &names) const
{
	names.clear();
	PNET_DISPLAY_GROUP sorted_buffer = NULL;
	DWORD returned_entry_count = 0;

	NET_API_STATUS result;
	DWORD next_index = 0;

	try
	{
		result = NetQueryDisplayInformation(m_server_name, 3, start_index, 
			entries_requested, 0xFFFFFFFF, &returned_entry_count, 
			(void **) &sorted_buffer);

		if ( (result!=NERR_Success) && (result!=ERROR_MORE_DATA) ) {
			throw NTMetaDataException(L"NetQueryDisplayInformation", result);
		}
	
		for (DWORD i=0; i<returned_entry_count; i++) 
		{
			names.insert(names.end(), sorted_buffer[i].grpi3_name);

			if ((result==ERROR_MORE_DATA) && (i == returned_entry_count-1)) {
				next_index = sorted_buffer[i].grpi3_next_index;
			}
		}

		NetApiBufferFree(sorted_buffer);

	} catch (NTMetaDataException)
	{
		if (sorted_buffer) NetApiBufferFree(sorted_buffer);
		throw;
	}

	return next_index;
}



// Not used in the current DLL functionality implementation
/*
const vector<GroupInfo> NTMetaData::GetAllGlobalGroupsData() const
{
	vector<GroupInfo> groups;
	DWORD entries_read, total_entries, resume_handle = 0;
	GROUP_INFO_1 *group_result = NULL;
	NET_API_STATUS result;
	try
	{	
		do 
		{			
			result = NetGroupEnum(m_server_name, 1, (LPBYTE*) &group_result, NET_BUF_SIZE,
								  &entries_read, &total_entries, &resume_handle);

			if ( (result!=NERR_Success) && (result!=ERROR_MORE_DATA) ) 
				throw NTMetaDataException(L"NetGroupEnum",result);

			for (DWORD i=0; i<entries_read; i++) 
			{
				GroupInfo group(group_result[i].grpi1_name, group_result[i].grpi1_comment, false);
				groups.insert(groups.end(),group);
			}			
			NetApiBufferFree(group_result);

		} while (result==ERROR_MORE_DATA);
	} catch (NTMetaDataException)
	{
		if (group_result) NetApiBufferFree(group_result);
		throw;
	}
	
	return groups;
}
*/


const vector<Wrapper_LPWSTR> NTMetaData::GetGlobalGroupUsers(Wrapper_LPWSTR p_group_name) const
{
	vector<Wrapper_LPWSTR> members;
	DWORD entries_read, total_entries;
	DWORD_PTR resume_handle = NULL;
	GROUP_USERS_INFO_0 *member_result = NULL;
	NET_API_STATUS result;

	try
	{
		do 
		{   
			result = NetGroupGetUsers(m_server_name, p_group_name, 0, (LPBYTE*) &member_result, 
									  NET_BUF_SIZE, &entries_read, &total_entries, &resume_handle);

			if ((result!=NERR_Success) && (result!=ERROR_MORE_DATA))
				throw NTMetaDataException(L"NetGroupGetUsers",result);		


			for (DWORD i=0; i<entries_read; i++) 
			{
				members.insert(members.end(),member_result[i].grui0_name);
			}
			
			NetApiBufferFree(member_result);

		} while (result==ERROR_MORE_DATA);
	} catch (NTMetaDataException)
	{
		if (member_result) NetApiBufferFree(member_result);
		throw;
	}
	return members;
}



const GroupInfo NTMetaData::GlobalGroupGetInfo(Wrapper_LPWSTR p_group_name) const
{
	GROUP_INFO_1 *group_result = NULL;
	try
	{
		NET_API_STATUS result = NetGroupGetInfo(m_server_name, p_group_name, 1, (LPBYTE*) &group_result);

		if (result!=NERR_Success)
			throw NTMetaDataException(L"NetGroupGetInfo", result);

	} catch (NTMetaDataException)
	{
		if (group_result) NetApiBufferFree(group_result);
		throw;
	}
	GroupInfo info(*group_result);
	NetApiBufferFree(group_result);

	return info;
}



// Modyfying Functions

void NTMetaData::GlobalGroupAdd(GroupInfo p_info) const
{	
	NET_API_STATUS result = NetGroupAdd(m_server_name, 1, (LPBYTE) &p_info.GetGroupInfo1(), NULL);
	if (result!=NERR_Success)
		throw NTMetaDataException(L"NetGroupAdd",result);
}



void NTMetaData::GlobalGroupAddUser(Wrapper_LPWSTR p_group_name, Wrapper_LPWSTR p_user_name) const
{
	NET_API_STATUS result = NetGroupAddUser(m_server_name, p_group_name, p_user_name);
	if (result!=NERR_Success)
		throw NTMetaDataException(L"NetGroupAddUser",result);
}



void NTMetaData::GlobalGroupDel(Wrapper_LPWSTR p_name) const
{
	NET_API_STATUS result = NetGroupDel(m_server_name, p_name);
	if (result!=NERR_Success)
		throw NTMetaDataException(L"NetGroupDel",result);
}



void NTMetaData::GlobalGroupDelUser(Wrapper_LPWSTR p_group_name, Wrapper_LPWSTR p_user_name) const
{
	NET_API_STATUS result = NetGroupDelUser(m_server_name, p_group_name, p_user_name);
	if (result!=NERR_Success)
		throw NTMetaDataException(L"NetGroupDelUser",result);
}



void NTMetaData::GlobalGroupSetInfo(Wrapper_LPWSTR p_group_name, GroupInfo p_info) const
// RENAME functionality is capsulated in this function
{
	NET_API_STATUS result = NetGroupSetInfo(m_server_name, p_group_name, 1, (LPBYTE) &p_info.GetGroupInfo1(), NULL);
	if (result!=NERR_Success)
		throw NTMetaDataException(L"NetGroupSetInfo",result);

	//Renaming
	if (!wstring(p_info.name).empty()&&wstring(p_info.name.ToUpperCase()).compare((wstring)p_group_name.ToUpperCase()))
	{
		result = NetGroupSetInfo(m_server_name, p_group_name, 0, (LPBYTE) &p_info.GetGroupInfo0(), NULL);
		if (result!=NERR_Success)
			throw NTMetaDataException(L"NetGroupSetInfo",result);
	}
}



void NTMetaData::GlobalGroupSetUsers(Wrapper_LPWSTR p_group_name,  const std::vector<Wrapper_LPWSTR> &p_info) const
{
	GROUP_USERS_INFO_0 *info = new GROUP_USERS_INFO_0[p_info.size()];
	for (std::vector<Wrapper_LPWSTR>::size_type i=0; i< p_info.size(); i++)
	{
		info[i].grui0_name = *((Wrapper_LPWSTR*) &p_info[i]);
	}
	NET_API_STATUS result = NetGroupSetUsers(m_server_name, p_group_name, 0, (LPBYTE) info, p_info.size());
	delete[] info;
	if (result!=NERR_Success)
		throw NTMetaDataException(L"NetGroupSetUsers",result);
}




// ---------------------
// Local Group Functions
// ---------------------



// Retrieving Functions

const vector<Wrapper_LPWSTR> NTMetaData::GetLocalGroupMembers(Wrapper_LPWSTR p_group_name, int p_members_type) const
{
	vector<Wrapper_LPWSTR> members;

	DWORD entries_read, total_entries;
	DWORD_PTR resume_handle = NULL;
	LOCALGROUP_MEMBERS_INFO_2 *member_result = NULL;

	NET_API_STATUS result;
	try
	{
		do 
		{		
			result = NetLocalGroupGetMembers(m_server_name, p_group_name, 2, (LPBYTE*) &member_result, 
											NET_BUF_SIZE, &entries_read, &total_entries, &resume_handle);

			if ( (result!=NERR_Success) && (result!=ERROR_MORE_DATA) )
				throw NTMetaDataException(L"NetLocalGroupGetMembers",result);		

		
			for (DWORD i=0; i<entries_read; i++) 
			{
				Wrapper_LPWSTR member_name(GetPureUserName(member_result[i].lgrmi2_domainandname));

				if ( (p_members_type == MEMBERS_TYPE_USERS_GROUPS ||
					  p_members_type == MEMBERS_TYPE_USERS) &&
					 (member_result[i].lgrmi2_sidusage==SidTypeUser) )
				{
					members.insert(members.end(), member_name);
				}

				if ( (p_members_type == MEMBERS_TYPE_USERS_GROUPS ||
					  p_members_type == MEMBERS_TYPE_GROUPS) &&
					 (member_result[i].lgrmi2_sidusage==SidTypeGroup) )
				{
					members.insert(members.end(), member_name);
				}
			}
			NetApiBufferFree(member_result);

		} while (result==ERROR_MORE_DATA);
	} catch (NTMetaDataException)
	{
		if (member_result) NetApiBufferFree(member_result);
		throw;
	}

	return members;
}



const vector<Wrapper_LPWSTR> NTMetaData::GetLocalGroupsNames() const
{
	vector<Wrapper_LPWSTR> groups;
	DWORD entries_read, total_entries;
	DWORD_PTR resume_handle = NULL;
	LOCALGROUP_INFO_0 *group_result = NULL;
	NET_API_STATUS result;
	try
	{	
		do 
		{
			result = NetLocalGroupEnum(m_server_name, 0,(LPBYTE*) &group_result, NET_BUF_SIZE,
									   &entries_read, &total_entries, &resume_handle);

			if ( (result!=NERR_Success) && (result!=ERROR_MORE_DATA) )  // Error occured
				throw NTMetaDataException(L"NetLocalGroupEnum",result);

			for (DWORD i=0; i<entries_read; i++)
			{
				groups.insert(groups.end(), group_result[i].lgrpi0_name);
			}			
			NetApiBufferFree(group_result);
		} while (result==ERROR_MORE_DATA);
	} catch (NTMetaDataException)
	{
		if (group_result) NetApiBufferFree(group_result);
		throw;
	}
	
	return groups;
}



// Not used in the current DLL functionality implementation
/*
const vector<GroupInfo> NTMetaData::GetAllLocalGroupsData() const
{
	vector<GroupInfo> groups;
	DWORD entries_read, total_entries, resume_handle = 0;
	LOCALGROUP_INFO_1 *group_result = NULL;
	NET_API_STATUS result;
	try
	{	
		do 
		{
			result = NetLocalGroupEnum(m_server_name, 1,(LPBYTE*) &group_result, NET_BUF_SIZE,
									   &entries_read, &total_entries, &resume_handle);

			if ( (result!=NERR_Success) && (result!=ERROR_MORE_DATA) )  // Error occured
				throw NTMetaDataException(L"NetLocalGroupEnum",result);

			for (DWORD i=0; i<entries_read; i++)
			{
				GroupInfo group(group_result[i].lgrpi1_name, group_result[i].lgrpi1_comment, false);
				groups.insert(groups.end(),group);
			}			
			NetApiBufferFree(group_result);

		} while (result==ERROR_MORE_DATA);
	} catch (NTMetaDataException)
	{
		if (group_result) NetApiBufferFree(group_result);
		throw;
	}
	
	return groups;
}
*/


const vector<Wrapper_LPWSTR> NTMetaData::GetLocalGroupUsers(Wrapper_LPWSTR p_group_name) const
{
	return GetLocalGroupMembers(p_group_name, MEMBERS_TYPE_USERS);
}



const vector<Wrapper_LPWSTR> NTMetaData::GetLocalGroupGlobalGroups(Wrapper_LPWSTR p_group_name) const
{
	return GetLocalGroupMembers(p_group_name, MEMBERS_TYPE_GROUPS);
}



const GroupInfo NTMetaData::LocalGroupGetInfo(Wrapper_LPWSTR p_group_name) const
{
	LOCALGROUP_INFO_1 *group_result = NULL;
	try
	{
		NET_API_STATUS result = NetLocalGroupGetInfo(m_server_name, p_group_name, 1, (LPBYTE*) &group_result);
		if (result != NERR_Success)
			throw NTMetaDataException(L"NetLocalGroupGetInfo", result);
	} catch (NTMetaDataException)
	{
		if (group_result) NetApiBufferFree(group_result);
		throw;
	}
	GroupInfo info(*group_result);
	NetApiBufferFree(group_result);

	return info;
}



// Modyfying Functions

void NTMetaData::LocalGroupAdd(GroupInfo p_info) const
{
	NET_API_STATUS result = NetLocalGroupAdd(m_server_name, 1, (LPBYTE) &p_info.GetLocalGroupInfo1(), NULL);
	if (result!=NERR_Success)
		throw NTMetaDataException(L"NetLocalGroupAdd",result);
}



void NTMetaData::LocalGroupAddMembers(Wrapper_LPWSTR p_local_group_name, const vector<Wrapper_LPWSTR> &p_info) const
{
	
	LOCALGROUP_MEMBERS_INFO_3 *info = new LOCALGROUP_MEMBERS_INFO_3[p_info.size()];
	for (std::vector<Wrapper_LPWSTR>::size_type i=0; i< p_info.size(); i++)
	{
		info[i].lgrmi3_domainandname = *((Wrapper_LPWSTR*) &p_info[i]);
	}
	NET_API_STATUS result = NetLocalGroupAddMembers(m_server_name, p_local_group_name, 3, (LPBYTE)info, p_info.size());
	delete[] info;
	if (result!=NERR_Success)
		throw NTMetaDataException(L"NetLocalGroupAddMembers",result);
}



void NTMetaData::LocalGroupAddUsers(Wrapper_LPWSTR p_local_group_name, const vector<Wrapper_LPWSTR> &p_info) const
{
	LocalGroupAddMembers(p_local_group_name, p_info);
}



void NTMetaData::LocalGroupAddGlobalGroups(Wrapper_LPWSTR p_local_group_name, const vector<Wrapper_LPWSTR> &p_info) const
{
	LocalGroupAddMembers(p_local_group_name, p_info);
}



void NTMetaData::LocalGroupDel(Wrapper_LPWSTR p_name) const
{
	NET_API_STATUS result = NetLocalGroupDel(m_server_name, p_name);
	if (result!=NERR_Success)
		throw NTMetaDataException(L"NetLocalGroupDel",result);
}



void NTMetaData::LocalGroupDelMembers(Wrapper_LPWSTR p_local_group_name, const vector<Wrapper_LPWSTR> &p_info) const
{
	LOCALGROUP_MEMBERS_INFO_3 *info = new LOCALGROUP_MEMBERS_INFO_3[p_info.size()];
	PRINT_TO_LOG(Wrapper_LPWSTR("LocalGroupDelMembers Dump Part1:") +
					 "\n\t p_local_group_name:" + p_local_group_name +
					 "\n\t m_server_name:" + m_server_name);
	for (std::vector<Wrapper_LPWSTR>::size_type i=0; i< p_info.size(); i++)
	{
		info[i].lgrmi3_domainandname = *((Wrapper_LPWSTR*) &p_info[i]);
		PRINT_TO_LOG(Wrapper_LPWSTR("LocalGroupDelMembers Dump Part2:") +
					 "\n\t p_info[i]:" + p_info[i] +
					 "\n\t info[i]:" + info[i].lgrmi3_domainandname);
	}
	NET_API_STATUS result = NetLocalGroupDelMembers(m_server_name, p_local_group_name, 3, (LPBYTE)info, p_info.size());
	delete[] info;
	if (result!=NERR_Success)
		throw NTMetaDataException(L"NetLocalGroupDelMembers",result);
}



void NTMetaData::LocalGroupDelUsers(Wrapper_LPWSTR p_local_group_name, const vector<Wrapper_LPWSTR> &p_info) const
{
	LocalGroupDelMembers(p_local_group_name, p_info);
}



void NTMetaData::LocalGroupDelGlobalGroups(Wrapper_LPWSTR p_local_group_name, const vector<Wrapper_LPWSTR> &p_info) const
{
	LocalGroupDelMembers(p_local_group_name, p_info);
}



void NTMetaData::LocalGroupSetInfo(Wrapper_LPWSTR p_name, GroupInfo p_info) const
// RENAME functionality is capsulated in this function
{

	PRINT_TO_LOG("LocalGroupSetInfo Dump:" + DumpGroupInfo(p_info));

	NET_API_STATUS result = NetLocalGroupSetInfo(m_server_name, p_name, 1, (LPBYTE) &p_info.GetLocalGroupInfo1(), NULL);
	if (result!=NERR_Success)
		throw NTMetaDataException(L"NetLocalGroupSetInfo",result);

	// Renaming
	if (!wstring(p_info.name).empty()&&wstring(p_info.name.ToUpperCase()).compare((wstring)p_name.ToUpperCase()))
	{	
		result = NetLocalGroupSetInfo(m_server_name, p_name, 0, (LPBYTE) &p_info.GetLocalGroupInfo0(), NULL);
		if (result!=NERR_Success)
			throw NTMetaDataException(L"NetLocalGroupSetInfo",result);
	}
}



void NTMetaData::LocalGroupSetMembers(Wrapper_LPWSTR p_local_group_name, const vector<Wrapper_LPWSTR> &p_info) const
{	
	LOCALGROUP_MEMBERS_INFO_3 *info = new LOCALGROUP_MEMBERS_INFO_3[p_info.size()];
	PRINT_TO_LOG(Wrapper_LPWSTR("LocalGroupSetMembers Dump Part1:") +
					 "\n\t m_server_name" + m_server_name +
					 "\n\t p_local_group_name:" + p_local_group_name);
	for (std::vector<Wrapper_LPWSTR>::size_type i=0; i< p_info.size(); i++)
	{
		info[i].lgrmi3_domainandname = *((Wrapper_LPWSTR*) &p_info[i]);
		PRINT_TO_LOG(Wrapper_LPWSTR("LocalGroupSetMembers Dump Part2:") +
					 "\n\t p_info[i]:" + p_info[i] +
					 "\n\t info[i]:" + info[i].lgrmi3_domainandname);
	}
	NET_API_STATUS result = NetLocalGroupSetMembers(m_server_name, p_local_group_name, 3, (LPBYTE) info, p_info.size());
	delete[] info;
	if (result!=NERR_Success)
		throw NTMetaDataException(L"NetLocalGroupSetMembers", result);
}



// --------------
// User Functions
// --------------



// Retrieving Functions

const UserInfo NTMetaData::UserGetInfo(Wrapper_LPWSTR p_user_name) const
{

	USER_INFO_3 *user_result = NULL;
	
	try 
	{
		NET_API_STATUS result = NetUserGetInfo(m_server_name, p_user_name, 3, (LPBYTE *) &user_result);

		if (result!=NERR_Success)
			throw NTMetaDataException(L"NetUserGetInfo",result);

	} catch (NTMetaDataException)
	{
		if (user_result) NetApiBufferFree(user_result);
		throw;
	}
	UserInfo user(user_result, GetUserPrimaryGroup(user_result->usri3_primary_group_id));
	NetApiBufferFree(user_result);
	
	return user;
}



const DWORD NTMetaData::GetUsersNames(DWORD start_index, 
									  DWORD entries_requested,
									  vector<Wrapper_LPWSTR> &names) const
{
	names.clear();
	PNET_DISPLAY_USER sorted_buffer = NULL;
	DWORD returned_entry_count = 0;

	NET_API_STATUS result;
	DWORD next_index = 0;

	try
	{
		result = NetQueryDisplayInformation(m_server_name, 1, start_index, 
			entries_requested, 0xFFFFFFFF, &returned_entry_count, 
			(void **) &sorted_buffer);

		if ( (result!=NERR_Success) && (result!=ERROR_MORE_DATA) ) {
			throw NTMetaDataException(L"NetQueryDisplayInformation", result);
		}
	
		for (DWORD i=0; i<returned_entry_count; i++) 
		{
			names.insert(names.end(), sorted_buffer[i].usri1_name);

			if ((result==ERROR_MORE_DATA) && (i == returned_entry_count-1)) {
				next_index = sorted_buffer[i].usri1_next_index;
			}
		}

		NetApiBufferFree(sorted_buffer);

	} catch (NTMetaDataException)
	{
		if (sorted_buffer) NetApiBufferFree(sorted_buffer);
		throw;
	}

	return next_index;
}



// Not used in the current DLL functionality implementation
/*
const vector<UserInfo> NTMetaData::GetAllUsersData() const
{
	vector<UserInfo> users;
	DWORD entries_read, total_entries, resume_handle = 0;
	USER_INFO_3 *user_result = NULL;

	NET_API_STATUS result;

	try
	{
		do 
		{
			result = NetUserEnum(m_server_name, 3, 
				FILTER_NORMAL_ACCOUNT, (LPBYTE*) &user_result, 
				NET_BUF_SIZE, &entries_read, &total_entries, &resume_handle);

			if ( (result!=NERR_Success) && (result!=ERROR_MORE_DATA) )
				throw NTMetaDataException(L"NetUserEnum",result);

			for (DWORD i=0; i<entries_read; i++) 
			{
				UserInfo user(&user_result[i], GetUserPrimaryGroup(user_result[i].usri3_primary_group_id));
				users.insert(users.end(),user);
			}

			NetApiBufferFree(user_result);

		} while (result==ERROR_MORE_DATA);
	} catch (NTMetaDataException)
	{
		if (user_result) NetApiBufferFree(user_result);
		throw;
	}

	return users;
}
*/


const vector<Wrapper_LPWSTR> NTMetaData::GetUserLocalGroups(Wrapper_LPWSTR p_user_name) const
{
	vector<Wrapper_LPWSTR> groups;
	DWORD local_entries_read, local_total_entries;
	LOCALGROUP_USERS_INFO_0 *local_group_result = NULL;
	
	try
	{
		NET_API_STATUS local_result = NetUserGetLocalGroups(m_server_name, p_user_name, 0, 0,
			(LPBYTE*) &local_group_result, (DWORD)-1, &local_entries_read, &local_total_entries);

		if ((local_result!=NERR_Success) && (local_result!=ERROR_MORE_DATA))
			throw NTMetaDataException(L"NetUserGetLocalGroups", local_result);

		for (std::vector<Wrapper_LPWSTR>::size_type i=0; i<local_entries_read; i++) groups.insert(groups.end(), local_group_result[i].lgrui0_name);

		NetApiBufferFree(local_group_result);

	} catch (NTMetaDataException)
	{
		if (local_group_result) NetApiBufferFree(local_group_result);
		throw;
	}
	
	return groups;
}



const vector<Wrapper_LPWSTR> NTMetaData::GetUserGlobalGroups(Wrapper_LPWSTR p_user_name) const
{
	vector<Wrapper_LPWSTR> groups;
	DWORD global_entries_read, global_total_entries;
	//LOCALGROUP_USERS_INFO_0 *local_group_result = NULL;
	GROUP_USERS_INFO_0 *global_group_result = NULL;
	
	try
	{

		NET_API_STATUS global_result = NetUserGetGroups(m_server_name, p_user_name, 0,
			(LPBYTE*) &global_group_result, (DWORD)-1, &global_entries_read, &global_total_entries);

		if ((global_result!=NERR_Success) && (global_result!=ERROR_MORE_DATA))
			throw NTMetaDataException(L"NetUserGetGroups", global_result);

		for (std::vector<Wrapper_LPWSTR>::size_type i=0; i<global_entries_read; i++) groups.insert(groups.end(), global_group_result[i].grui0_name);

		NetApiBufferFree(global_group_result);

	} catch (NTMetaDataException)
	{
		if (global_group_result) NetApiBufferFree(global_group_result);
		throw;
	}
	
	return groups;
}



const Wrapper_LPWSTR NTMetaData::GetUserPrimaryGroup(DWORD p_primary_group_id) const
{
	DWORD entries_read, total_entries;
	DWORD_PTR resume_handle = NULL;
	GROUP_INFO_2 *group_result = NULL;
	NET_API_STATUS result;
	Wrapper_LPWSTR primary_group_name;

	try
	{	
		do 
		{			
			result = NetGroupEnum(m_server_name, 2, (LPBYTE*) &group_result, NET_BUF_SIZE,
					&entries_read, &total_entries, &resume_handle);

			if ( (result!=NERR_Success) && (result!=ERROR_MORE_DATA) ) 
				return L"";

			for (DWORD i=0; i<entries_read; i++) 
				if (group_result[i].grpi2_group_id == p_primary_group_id)
				{
					primary_group_name = group_result[i].grpi2_name;				
				}			
			NetApiBufferFree(group_result);

		} while (result==ERROR_MORE_DATA);
	} catch (NTMetaDataException)
	{
		if (group_result) NetApiBufferFree(group_result);
		throw;
	}

	return primary_group_name;
}



// Modyfying Functions

void NTMetaData::UserAdd(UserInfo p_info) const
{
	DWORD parm_err = 0;
	PRINT_TO_LOG(DumpUserInfo(p_info) + 
				 "\n\nm_server_name:" + m_server_name);
	p_info.primary_group_id = DOMAIN_GROUP_RID_USERS; // Required for NetUserAdd
	NET_API_STATUS result = NetUserAdd(m_server_name, 3, (LPBYTE) &p_info.GetUserInfo3(), &parm_err);
	if (result!=NERR_Success)
		throw NTMetaDataException(L"NetUserAdd", result, &parm_err);
}



void NTMetaData::UserChangePassword(Wrapper_LPWSTR p_user_name, Wrapper_LPWSTR p_old_psw, Wrapper_LPWSTR p_new_psw) const
{
	NET_API_STATUS result = NetUserChangePassword(m_server_name, p_user_name, p_old_psw, p_new_psw);
	if (result!=NERR_Success)
		throw NTMetaDataException(L"NetUserChangePassword",result);
}



void NTMetaData::UserDel(Wrapper_LPWSTR p_name) const
{
	NET_API_STATUS result = NetUserDel(m_server_name, p_name);
	if (result!=NERR_Success)
		throw NTMetaDataException(L"NetUserDel",result);
}



void NTMetaData::UserSetGlobalGroups(Wrapper_LPWSTR p_user_name,  const std::vector<Wrapper_LPWSTR> &p_info) const
{
	GROUP_USERS_INFO_0 *info = new GROUP_USERS_INFO_0[p_info.size()];
	PRINT_TO_LOG(Wrapper_LPWSTR("UserSetGlobalGroups Dump Part1:") + 
				 "\n\t p_user_name:" + p_user_name +
				 "\n\t m_server_name:" + m_server_name)
	for (std::vector<Wrapper_LPWSTR>::size_type i=0; i< p_info.size(); i++)
	{
		info[i].grui0_name = *((Wrapper_LPWSTR*) &p_info[i]);
		PRINT_TO_LOG(Wrapper_LPWSTR("UserSetGlobalGroups Dump Part2:") +
					 "\n\t p_info[i]:" + p_info[i] +
					 "\n\t info[i]:" + info[i].grui0_name);
	}
	NET_API_STATUS result = NetUserSetGroups(m_server_name, p_user_name, 0, (LPBYTE) info, p_info.size());
	delete[] info;
	if (result!=NERR_Success)
		throw NTMetaDataException(L"NetUserSetGroups",result);

}



void NTMetaData::UserSetInfo(Wrapper_LPWSTR p_user_name, UserInfo p_info) const
// RENAME functionality is capsulated in this function
// Warning this function ignores password only if it set to NULL 
{
	NET_API_STATUS	result = NetUserSetInfo(m_server_name, p_user_name, 3, (LPBYTE) &p_info.GetUserInfo3(), NULL);
	if (result!=NERR_Success)
		throw NTMetaDataException(L"NetUserSetInfo",result);

	// Renaming
	if (!wstring(p_info.name).empty()&&wstring(p_info.name.ToUpperCase()).compare((wstring)p_user_name.ToUpperCase()))
	{
		result = NetUserSetInfo(m_server_name, p_user_name, 0, (LPBYTE) &p_info.GetUserInfo0(), NULL);
		if (result!=NERR_Success)
			throw NTMetaDataException(L"NetUserSetInfo",result);
	}
}



void NTMetaData::UserSetPrimaryGroup(Wrapper_LPWSTR p_user_name, Wrapper_LPWSTR p_group_name) const
{
	NET_API_STATUS result = NetGroupAddUser(m_server_name, p_group_name, p_user_name);
	if ((result != NERR_UserInGroup) && (result != NERR_Success))
		throw NTMetaDataException(L"NetGroupAddUser", result);
	USER_INFO_1051 info;
	info.usri1051_primary_group_id = GetGroupId(p_group_name);
	result = NetUserSetInfo(m_server_name, p_user_name, 1051, (LPBYTE) &info, NULL);
	if (result!=NERR_Success)
		throw NTMetaDataException(L"NetUserSetInfo", result);
}


const Wrapper_LPWSTR NTMetaData::UserGetPrimaryGroup(Wrapper_LPWSTR p_user_name) const
{
	UserInfo ui = UserGetInfo(p_user_name);
	return ui.primary_group_name;
}


DWORD NTMetaData::GetGroupId(Wrapper_LPWSTR p_group_name) const 
{
	GROUP_INFO_2 *info = NULL;
	DWORD group_id = 0;
	try
	{
		NET_API_STATUS result = NetGroupGetInfo(m_server_name, p_group_name, 2, (LPBYTE*) &info);
		if (result!=NERR_Success)
			throw NTMetaDataException(L"NetGroupGetInfo",result);
		group_id = info->grpi2_group_id;
		NetApiBufferFree(info);
		
	} catch (NTMetaDataException)
	{
		if (info) NetApiBufferFree(info);
	}
	return group_id;
}




// *****************************************************************
// implementation of utils not exported in the DLL (not used so far)
// *****************************************************************



const Wrapper_LPWSTR GetLocalMachineName()
{
	DWORD buf_size = MAX_COMPUTERNAME_LENGTH + 1;
	WCHAR buf[MAX_COMPUTERNAME_LENGTH + 1];
	if (! GetComputerName((LPTSTR) &buf, &buf_size))
		throw NTMetaDataException(L"GetComputerName", GetLastError());
	
	return buf;
}



const Wrapper_LPWSTR GetDomainControllerComputerName()
{
	LPCWSTR buf;
	try 
	{

		NET_API_STATUS result = NetGetDCName(NULL, NULL, (LPBYTE*) &buf);
		if ( result != NERR_Success )
			throw NTMetaDataException(L"NetGetDCName",result);
	} catch (NTMetaDataException)
	{
		NetApiBufferFree((LPVOID) buf);
		throw;
	}
	Wrapper_LPWSTR DCname(buf);
	NetApiBufferFree((LPVOID) buf);
	
	return DCname;
}



// Returns the domain and username of currently logged in Windows NT/2000 user
void GetDomainAndUserName(Wrapper_LPWSTR &p_domain_name,Wrapper_LPWSTR &p_user_name)
{
    HANDLE hProcess, hAccessToken; 
	TOKEN_USER token_information;
    WCHAR account_name[MAX_ACCOUNT_NAME_LEN], domain_name[MAX_DOMAIN_NAME_LEN];
    DWORD token_information_return_length, account_size=MAX_ACCOUNT_NAME_LEN, domain_size=MAX_DOMAIN_NAME_LEN; 
    SID_NAME_USE snu; 

    hProcess = GetCurrentProcess(); 
    if (! OpenProcessToken(hProcess,TOKEN_READ,&hAccessToken))
		throw NTMetaDataException(L"OpenProcessToken", GetLastError());

    if (! GetTokenInformation(hAccessToken, TokenUser, &token_information, sizeof(token_information), 
		                      &token_information_return_length))
		throw NTMetaDataException(L"GetTokenInformation", GetLastError());

    if (! LookupAccountSid(NULL, token_information.User.Sid, account_name, &account_size, 
		                   domain_name, &domain_size, &snu))
		throw NTMetaDataException(L"LookupAccountSid", GetLastError());

	p_domain_name = domain_name;
	p_user_name = account_name;
}



// Returns the domain name for the currently logged in Windows 2000 user
const Wrapper_LPWSTR GetDomainName() 
{
	Wrapper_LPWSTR domain_name, user_name;
	GetDomainAndUserName(domain_name,user_name);

	return domain_name;
}




// ****************************************************************
// JNI layer implementation
// ****************************************************************


#define SAFE_JNI_STRING_PARAM(mp_jni_param_name) Wrapper_LPWSTR mp_jni_param_name;\
	if (p_##mp_jni_param_name) { \
		const char* utfString##mp_jni_param_name = p_env->GetStringUTFChars(p_##mp_jni_param_name, NULL); \
		size_t charCount##mp_jni_param_name = _mbstrlen(utfString##mp_jni_param_name); \
		WCHAR *wcs_##mp_jni_param_name = (WCHAR *) malloc(sizeof(WCHAR) * (charCount##mp_jni_param_name + 1)); \
		int resultCode##mp_jni_param_name = MultiByteToWideChar(CP_UTF8, 0, utfString##mp_jni_param_name, -1, wcs_##mp_jni_param_name, charCount##mp_jni_param_name + 1); \
		if(resultCode##mp_jni_param_name > 0) { \
			mp_jni_param_name.initFromJNI(wcs_##mp_jni_param_name, charCount##mp_jni_param_name); \
		} \
		p_env->ReleaseStringUTFChars(p_##mp_jni_param_name, utfString##mp_jni_param_name); \
		free(wcs_##mp_jni_param_name); \
	}

#define JNI_CATCH_DEFAULT_EXCEPTION catch (...) \
	{ \
		if (!p_env->ExceptionOccurred()) \
		{ \
			jclass newExcCls = p_env->FindClass(NTMetaDataException_JavaClassName); \
			p_env->ThrowNew(newExcCls, NTMetaDataException::FormatAPIErrorCode(L"unknown")); \
		} \
	} 

#define JNI_CATCH_NTMetaDataException  catch (NTMetaDataException e) \
	{ \
		jclass newExcCls = p_env->FindClass(NTMetaDataException_JavaClassName); \
		p_env->ThrowNew(newExcCls, (Wrapper_LPWSTR)e.getMessage()); \
	} \



JNIEXPORT jboolean JNICALL Java_com_ibm_di_connector_NT4UserMetaDataConnector_NTMetaData_logOn
  (JNIEnv *p_env, jclass, jstring p_computer_name, jstring p_user_name, jstring p_password)
{
	try
	{
		SAFE_JNI_STRING_PARAM(computer_name);
		SAFE_JNI_STRING_PARAM(user_name);
		SAFE_JNI_STRING_PARAM(password);

		NTMetaData md(computer_name);

		return md.LogOn(user_name, password);

	} JNI_CATCH_NTMetaDataException 
	  JNI_CATCH_DEFAULT_EXCEPTION
	return false;
}



JNIEXPORT jboolean JNICALL Java_com_ibm_di_connector_NT4UserMetaDataConnector_NTMetaData_isPrimaryDomainController
  (JNIEnv *p_env, jclass, jstring p_computer_name)
{
	try
	{
		SAFE_JNI_STRING_PARAM(computer_name);

		NTMetaData md(computer_name);

		return md.IsPrimaryDomainController();
	} JNI_CATCH_NTMetaDataException
	  JNI_CATCH_DEFAULT_EXCEPTION
	return false;
}



JNIEXPORT jobject JNICALL Java_com_ibm_di_connector_NT4UserMetaDataConnector_NTMetaData_userGetInfo
  (JNIEnv *p_env, jclass, jstring p_computer_name, jstring p_user_name)
{
	try
	{
		SAFE_JNI_STRING_PARAM(computer_name);
		SAFE_JNI_STRING_PARAM(user_name);

		NTMetaData md(computer_name);

		jobject result = md.UserGetInfo(user_name).GetJObject(p_env);
		
		return result;
	} JNI_CATCH_NTMetaDataException
	  JNI_CATCH_DEFAULT_EXCEPTION
	return NULL;
}



JNIEXPORT jint JNICALL Java_com_ibm_di_connector_NT4UserMetaDataConnector_NTMetaData_getUsersNames
  (JNIEnv *p_env, jclass, jstring p_computer_name, jint p_start_index, 
	jint p_entries_requested, jobject p_users_names)
{
	try 
	{
		SAFE_JNI_STRING_PARAM(computer_name);

		NTMetaData md(computer_name);

		std::vector<Wrapper_LPWSTR> names;
		jint next_index = md.GetUsersNames(p_start_index, p_entries_requested, names);

		JavaVector users_names(p_env, p_users_names);
		users_names.clear();

		for (std::vector<Wrapper_LPWSTR>::size_type i=0; i<names.size(); i++) 
		{
			users_names.addElement(names[i].GetJObject(p_env));
		};
	
		return next_index;
	} JNI_CATCH_NTMetaDataException
	  JNI_CATCH_DEFAULT_EXCEPTION

	return NULL;
}



JNIEXPORT jobject JNICALL Java_com_ibm_di_connector_NT4UserMetaDataConnector_NTMetaData_getUserLocalGroups
  (JNIEnv *p_env, jclass, jstring p_computer_name, jstring p_user_name)
{
	try
	{
		SAFE_JNI_STRING_PARAM(computer_name);
		SAFE_JNI_STRING_PARAM(user_name);
		
		NTMetaData md(computer_name);

		jobject result = Conv_Vector_To_JavaVector(p_env, md.GetUserLocalGroups(user_name));

		return result;
	} JNI_CATCH_NTMetaDataException
	  JNI_CATCH_DEFAULT_EXCEPTION
	return NULL;
}



JNIEXPORT jobject JNICALL Java_com_ibm_di_connector_NT4UserMetaDataConnector_NTMetaData_getUserGlobalGroups
  (JNIEnv *p_env, jclass, jstring p_computer_name, jstring p_user_name)
{
	try
	{
		SAFE_JNI_STRING_PARAM(computer_name);
		SAFE_JNI_STRING_PARAM(user_name);
		
		NTMetaData md(computer_name);

		jobject result = Conv_Vector_To_JavaVector(p_env, md.GetUserGlobalGroups(user_name));

		return result;
	} JNI_CATCH_NTMetaDataException
	  JNI_CATCH_DEFAULT_EXCEPTION
	return NULL;
}



JNIEXPORT void JNICALL Java_com_ibm_di_connector_NT4UserMetaDataConnector_NTMetaData_userAdd
  (JNIEnv *p_env, jclass, jstring p_computer_name, jobject p_user_info)
{
	try
	{
		SAFE_JNI_STRING_PARAM(computer_name);		
		UserInfo info(p_env, p_user_info);
		PRINT_TO_LOG(DumpUserInfo(info));

		NTMetaData md(computer_name);

		md.UserAdd(info);

	} JNI_CATCH_NTMetaDataException
	  JNI_CATCH_DEFAULT_EXCEPTION
}



JNIEXPORT void JNICALL Java_com_ibm_di_connector_NT4UserMetaDataConnector_NTMetaData_userDel
  (JNIEnv *p_env, jclass, jstring p_computer_name, jstring p_user_name)
{
	try
	{
		SAFE_JNI_STRING_PARAM(computer_name);
		SAFE_JNI_STRING_PARAM(user_name);

		NTMetaData md(computer_name);

		md.UserDel(user_name);

	} JNI_CATCH_NTMetaDataException
	  JNI_CATCH_DEFAULT_EXCEPTION
}



JNIEXPORT void JNICALL Java_com_ibm_di_connector_NT4UserMetaDataConnector_NTMetaData_userSetInfo
  (JNIEnv *p_env, jclass, jstring p_computer_name, jstring p_user_name, jobject p_user_info)
{
	try
	{
		SAFE_JNI_STRING_PARAM(computer_name);
		SAFE_JNI_STRING_PARAM(user_name);

		UserInfo info(p_env, p_user_info);

		NTMetaData md(computer_name);

		md.UserSetInfo(user_name, info);

	} JNI_CATCH_NTMetaDataException
	  JNI_CATCH_DEFAULT_EXCEPTION
}



JNIEXPORT void JNICALL Java_com_ibm_di_connector_NT4UserMetaDataConnector_NTMetaData_userSetGlobalGroups
  (JNIEnv *p_env, jclass, jstring p_computer_name, jstring p_user_name, jobject p_groups)
{
	try
	{
		SAFE_JNI_STRING_PARAM(computer_name);
		SAFE_JNI_STRING_PARAM(user_name);

		NTMetaData md(computer_name);

		md.UserSetGlobalGroups(user_name, Conv_JavaVectorOfStrings_To_VectorOfWrapper_LPWSTR(p_env, p_groups));

	} JNI_CATCH_NTMetaDataException
	  JNI_CATCH_DEFAULT_EXCEPTION
}



JNIEXPORT void JNICALL Java_com_ibm_di_connector_NT4UserMetaDataConnector_NTMetaData_userSetPrimaryGroup
  (JNIEnv *p_env, jclass, jstring p_computer_name, jstring p_user_name, jstring p_primary_group)
{
	try
	{
		SAFE_JNI_STRING_PARAM(computer_name);
		SAFE_JNI_STRING_PARAM(user_name);
		SAFE_JNI_STRING_PARAM(primary_group);

		NTMetaData md(computer_name);

		md.UserSetPrimaryGroup(user_name, primary_group);
	} JNI_CATCH_NTMetaDataException
	  JNI_CATCH_DEFAULT_EXCEPTION
}



JNIEXPORT jstring JNICALL Java_com_ibm_di_connector_NT4UserMetaDataConnector_NTMetaData_userGetPrimaryGroup
  (JNIEnv * p_env, jclass, jstring p_computer_name, jstring p_user_name)
{
	try
	{
		SAFE_JNI_STRING_PARAM(computer_name);
		SAFE_JNI_STRING_PARAM(user_name);

		NTMetaData md(computer_name);

		return (jstring) md.UserGetPrimaryGroup(user_name).GetJObject(p_env);
	} JNI_CATCH_NTMetaDataException
	  JNI_CATCH_DEFAULT_EXCEPTION
	return NULL;
}



JNIEXPORT jobject JNICALL Java_com_ibm_di_connector_NT4UserMetaDataConnector_NTMetaData_globalGroupGetInfo
  (JNIEnv *p_env, jclass, jstring p_computer_name, jstring p_global_group_name)
{
	try
	{
		SAFE_JNI_STRING_PARAM(computer_name);
		SAFE_JNI_STRING_PARAM(global_group_name);

		NTMetaData md(computer_name);

		jobject result = md.GlobalGroupGetInfo(global_group_name).GetJObject(p_env);

		return result;
	} JNI_CATCH_NTMetaDataException
	  JNI_CATCH_DEFAULT_EXCEPTION
	return NULL;
}



JNIEXPORT jint JNICALL Java_com_ibm_di_connector_NT4UserMetaDataConnector_NTMetaData_getGlobalGroupsNames
  (JNIEnv *p_env, jclass, jstring p_computer_name, jint p_start_index, 
	jint p_entries_requested, jobject p_global_groups_names)
{
	try 
	{
		SAFE_JNI_STRING_PARAM(computer_name);

		NTMetaData md(computer_name);

		std::vector<Wrapper_LPWSTR> names;
		jint next_index = md.GetGlobalGroupsNames(p_start_index, p_entries_requested, names);
		
		JavaVector global_groups_names(p_env, p_global_groups_names);
		global_groups_names.clear();

		for (std::vector<Wrapper_LPWSTR>::size_type i=0; i<names.size(); i++) 
		{
			global_groups_names.addElement(names[i].GetJObject(p_env));
		};
	
		return next_index;
	} JNI_CATCH_NTMetaDataException
	  JNI_CATCH_DEFAULT_EXCEPTION

	return NULL;
}



JNIEXPORT jobject JNICALL Java_com_ibm_di_connector_NT4UserMetaDataConnector_NTMetaData_getGlobalGroupUsers
  (JNIEnv *p_env, jclass, jstring p_computer_name, jstring p_group_name)
{
	try
	{
		SAFE_JNI_STRING_PARAM(computer_name);
		SAFE_JNI_STRING_PARAM(group_name);

		NTMetaData md(computer_name);

		jobject result = Conv_Vector_To_JavaVector(p_env, md.GetGlobalGroupUsers(group_name));

		return result;
	} JNI_CATCH_NTMetaDataException
	  JNI_CATCH_DEFAULT_EXCEPTION
	return NULL;
}



JNIEXPORT void JNICALL Java_com_ibm_di_connector_NT4UserMetaDataConnector_NTMetaData_globalGroupAdd
  (JNIEnv *p_env, jclass, jstring p_computer_name, jobject p_group_info)
{
	try
	{
		SAFE_JNI_STRING_PARAM(computer_name);
		GroupInfo info(p_env, p_group_info);

		NTMetaData md(computer_name);

		md.GlobalGroupAdd(info);

	} JNI_CATCH_NTMetaDataException
	  JNI_CATCH_DEFAULT_EXCEPTION
}


JNIEXPORT void JNICALL Java_com_ibm_di_connector_NT4UserMetaDataConnector_NTMetaData_globalGroupAddUser
  (JNIEnv *p_env, jclass, jstring p_computer_name, jstring p_group_name, jstring p_user_name)
{
	try
	{
		SAFE_JNI_STRING_PARAM(computer_name);
		SAFE_JNI_STRING_PARAM(group_name);
		SAFE_JNI_STRING_PARAM(user_name);

		NTMetaData md(computer_name);

		md.GlobalGroupAddUser(group_name, user_name);

	} JNI_CATCH_NTMetaDataException
	  JNI_CATCH_DEFAULT_EXCEPTION
}



JNIEXPORT void JNICALL Java_com_ibm_di_connector_NT4UserMetaDataConnector_NTMetaData_globalGroupDel
  (JNIEnv *p_env, jclass, jstring p_computer_name, jstring p_group_name)
{
	try
	{
		SAFE_JNI_STRING_PARAM(computer_name);
		SAFE_JNI_STRING_PARAM(group_name);

		NTMetaData md(computer_name);

		md.GlobalGroupDel(group_name);

	} JNI_CATCH_NTMetaDataException
	  JNI_CATCH_DEFAULT_EXCEPTION
}



JNIEXPORT void JNICALL Java_com_ibm_di_connector_NT4UserMetaDataConnector_NTMetaData_globalGroupDelUser
  (JNIEnv *p_env, jclass, jstring p_computer_name, jstring p_group_name, jstring p_user_name)
{
	try
	{
		SAFE_JNI_STRING_PARAM(computer_name);
		SAFE_JNI_STRING_PARAM(group_name);
		SAFE_JNI_STRING_PARAM(user_name);

		NTMetaData md(computer_name);

		md.GlobalGroupDelUser(group_name, user_name);

	} JNI_CATCH_NTMetaDataException
	  JNI_CATCH_DEFAULT_EXCEPTION
}



JNIEXPORT void JNICALL Java_com_ibm_di_connector_NT4UserMetaDataConnector_NTMetaData_globalGroupSetUsers
  (JNIEnv *p_env, jclass, jstring p_computer_name, jstring p_name, jobject p_users)
{
	try
	{
		SAFE_JNI_STRING_PARAM(computer_name);
		SAFE_JNI_STRING_PARAM(name);

		NTMetaData md(computer_name);

		md.GlobalGroupSetUsers(name, Conv_JavaVectorOfStrings_To_VectorOfWrapper_LPWSTR(p_env, p_users));

	} JNI_CATCH_NTMetaDataException
	  JNI_CATCH_DEFAULT_EXCEPTION
}



JNIEXPORT void JNICALL Java_com_ibm_di_connector_NT4UserMetaDataConnector_NTMetaData_globalGroupSetInfo
  (JNIEnv *p_env, jclass, jstring p_computer_name, jstring p_group_name, jobject p_group_info)
{
	try
	{
		SAFE_JNI_STRING_PARAM(computer_name);
		SAFE_JNI_STRING_PARAM(group_name);
		GroupInfo info(p_env, p_group_info);

		NTMetaData md(computer_name);

		md.GlobalGroupSetInfo(group_name, info);

	} JNI_CATCH_NTMetaDataException
	  JNI_CATCH_DEFAULT_EXCEPTION
}



JNIEXPORT jobject JNICALL Java_com_ibm_di_connector_NT4UserMetaDataConnector_NTMetaData_localGroupGetInfo
  (JNIEnv *p_env, jclass, jstring p_computer_name, jstring p_group_name)
{
	try
	{
		SAFE_JNI_STRING_PARAM(computer_name);
		SAFE_JNI_STRING_PARAM(group_name);

		NTMetaData md(computer_name);

		jobject result = md.LocalGroupGetInfo(group_name).GetJObject(p_env);

		return result;
	} JNI_CATCH_NTMetaDataException
	  JNI_CATCH_DEFAULT_EXCEPTION
	return NULL;
}



JNIEXPORT jobject JNICALL Java_com_ibm_di_connector_NT4UserMetaDataConnector_NTMetaData_getLocalGroupsNames
  (JNIEnv *p_env, jclass, jstring p_computer_name)
{
	try
	{
		SAFE_JNI_STRING_PARAM(computer_name);

		NTMetaData md(computer_name);

		jobject result = Conv_Vector_To_JavaVector(p_env, md.GetLocalGroupsNames());

		return result;
	} JNI_CATCH_NTMetaDataException
	  JNI_CATCH_DEFAULT_EXCEPTION
	return NULL;
}



JNIEXPORT jobject JNICALL Java_com_ibm_di_connector_NT4UserMetaDataConnector_NTMetaData_getLocalGroupUsers
  (JNIEnv *p_env, jclass, jstring p_computer_name, jstring p_group_name)
{
	try
	{
		SAFE_JNI_STRING_PARAM(computer_name);
		SAFE_JNI_STRING_PARAM(group_name);

		NTMetaData md(computer_name);

		jobject result = Conv_Vector_To_JavaVector(p_env, md.GetLocalGroupUsers(group_name));

		return result;
	} JNI_CATCH_NTMetaDataException
	  JNI_CATCH_DEFAULT_EXCEPTION
	return NULL;
}



JNIEXPORT jobject JNICALL Java_com_ibm_di_connector_NT4UserMetaDataConnector_NTMetaData_getLocalGroupGlobalGroups
  (JNIEnv *p_env, jclass, jstring p_computer_name, jstring p_group_name)
{
	try
	{
		SAFE_JNI_STRING_PARAM(computer_name);
		SAFE_JNI_STRING_PARAM(group_name);

		NTMetaData md(computer_name);

		jobject result = Conv_Vector_To_JavaVector(p_env, md.GetLocalGroupGlobalGroups(group_name));

		return result;
	} JNI_CATCH_NTMetaDataException
	  JNI_CATCH_DEFAULT_EXCEPTION
	return NULL;
}



JNIEXPORT void JNICALL Java_com_ibm_di_connector_NT4UserMetaDataConnector_NTMetaData_localGroupAdd
  (JNIEnv *p_env, jclass, jstring p_computer_name, jobject p_group_info)
{
	try
	{
		SAFE_JNI_STRING_PARAM(computer_name);
		GroupInfo info(p_env, p_group_info);

		NTMetaData md(computer_name);

		md.LocalGroupAdd(info);

	} JNI_CATCH_NTMetaDataException
	  JNI_CATCH_DEFAULT_EXCEPTION
}



JNIEXPORT void JNICALL Java_com_ibm_di_connector_NT4UserMetaDataConnector_NTMetaData_localGroupDel
  (JNIEnv *p_env, jclass, jstring p_computer_name, jstring p_name)
{
	try
	{
		SAFE_JNI_STRING_PARAM(computer_name);
		SAFE_JNI_STRING_PARAM(name);

		NTMetaData md(computer_name);

		md.LocalGroupDel(name);

	} JNI_CATCH_NTMetaDataException
	  JNI_CATCH_DEFAULT_EXCEPTION
}



JNIEXPORT void JNICALL Java_com_ibm_di_connector_NT4UserMetaDataConnector_NTMetaData_localGroupAddUsers
  (JNIEnv *p_env, jclass, jstring p_computer_name, jstring p_name, jobject p_users)
{
	try
	{
		SAFE_JNI_STRING_PARAM(computer_name);
		SAFE_JNI_STRING_PARAM(name);

		NTMetaData md(computer_name);

		md.LocalGroupAddUsers(name, Conv_JavaVectorOfStrings_To_VectorOfWrapper_LPWSTR(p_env, p_users));

	} JNI_CATCH_NTMetaDataException
	  JNI_CATCH_DEFAULT_EXCEPTION
}



JNIEXPORT void JNICALL Java_com_ibm_di_connector_NT4UserMetaDataConnector_NTMetaData_localGroupAddGlobalGroups
  (JNIEnv *p_env, jclass, jstring p_computer_name, jstring p_name, jobject p_groups)
{
	try
	{
		SAFE_JNI_STRING_PARAM(computer_name);
		SAFE_JNI_STRING_PARAM(name);

		NTMetaData md(computer_name);

		md.LocalGroupAddGlobalGroups(name, Conv_JavaVectorOfStrings_To_VectorOfWrapper_LPWSTR(p_env, p_groups));

	} JNI_CATCH_NTMetaDataException
	  JNI_CATCH_DEFAULT_EXCEPTION
}



JNIEXPORT void JNICALL Java_com_ibm_di_connector_NT4UserMetaDataConnector_NTMetaData_localGroupDelUsers
  (JNIEnv *p_env, jclass, jstring p_computer_name, jstring p_name, jobject p_users)
{
	try
	{
		SAFE_JNI_STRING_PARAM(computer_name);
		SAFE_JNI_STRING_PARAM(name);

		NTMetaData md(computer_name);

		md.LocalGroupDelUsers(name, Conv_JavaVectorOfStrings_To_VectorOfWrapper_LPWSTR(p_env, p_users));

	} JNI_CATCH_NTMetaDataException
	  JNI_CATCH_DEFAULT_EXCEPTION
}



JNIEXPORT void JNICALL Java_com_ibm_di_connector_NT4UserMetaDataConnector_NTMetaData_localGroupDelGlobalGroups
  (JNIEnv *p_env, jclass, jstring p_computer_name, jstring p_name, jobject p_groups)
{
	try
	{
		SAFE_JNI_STRING_PARAM(computer_name);
		SAFE_JNI_STRING_PARAM(name);	

		NTMetaData md(computer_name);

		md.LocalGroupDelGlobalGroups(name, Conv_JavaVectorOfStrings_To_VectorOfWrapper_LPWSTR(p_env, p_groups));

	} JNI_CATCH_NTMetaDataException
	  JNI_CATCH_DEFAULT_EXCEPTION
}



JNIEXPORT void JNICALL Java_com_ibm_di_connector_NT4UserMetaDataConnector_NTMetaData_localGroupSetInfo
  (JNIEnv *p_env, jclass, jstring p_computer_name, jstring p_name, jobject p_group_info)
{
	try
	{
		SAFE_JNI_STRING_PARAM(computer_name);
		SAFE_JNI_STRING_PARAM(name);

		GroupInfo info(p_env, p_group_info);

		NTMetaData md(computer_name);

		md.LocalGroupSetInfo(name, info);

	} JNI_CATCH_NTMetaDataException
	  JNI_CATCH_DEFAULT_EXCEPTION
}



JNIEXPORT void JNICALL Java_com_ibm_di_connector_NT4UserMetaDataConnector_NTMetaData_localGroupSetMembers
  (JNIEnv *p_env, jclass, jstring p_computer_name, jstring p_name, jobject p_members)
{
	try
	{
		SAFE_JNI_STRING_PARAM(computer_name);
		SAFE_JNI_STRING_PARAM(name);

		NTMetaData md(computer_name);

		md.LocalGroupSetMembers(name, Conv_JavaVectorOfStrings_To_VectorOfWrapper_LPWSTR(p_env, p_members));

	} JNI_CATCH_NTMetaDataException
	  JNI_CATCH_DEFAULT_EXCEPTION
}
