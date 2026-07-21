// The NTMetaData.h defines classes that represent NT Data Structures (Groups and Users)
// and the wrapper class for WinAPI Net Functions (NTMetaData).

#ifndef INCLUDED_NTMETADATA_H
#define INCLUDED_NTMETADATA_H

#include <assert.h>
#include <windows.h>
#include <lm.h>
#include <string>
#include <vector>
#include "Wrapper_LPWSTR.h"
#include "NTMetaDataException.h"
#include "jni.h"
#include "JavaStructures.h"

#define SERVER_NAME_PREFIX L"\\\\"
#define LOGON_HOURS_ARRAY_LENGTH 21

struct GroupInfo : public JavaStruct
{
	Wrapper_LPWSTR name;
	Wrapper_LPWSTR comment;
	bool is_global;

private:
	union
	{
		GROUP_INFO_0 m_gi0;
		GROUP_INFO_1 m_gi1;
		LOCALGROUP_INFO_0 m_lgi0;
		LOCALGROUP_INFO_1 m_lgi1;
	};

	void SetGroupInfoUnion()
	{
		m_gi1.grpi1_name = name;
		m_gi1.grpi1_comment = comment;
	}

public:
    GroupInfo(const GroupInfo& p_info) :
		name(p_info.name), comment(p_info.comment), is_global(p_info.is_global) {}	

	GroupInfo(const Wrapper_LPWSTR p_name, const Wrapper_LPWSTR p_comment, bool p_is_global) :
		name(p_name), comment(p_comment), is_global(p_is_global) {}

	GroupInfo(const GROUP_INFO_1 p_info) :
		name(p_info.grpi1_name), comment(p_info.grpi1_comment), is_global(true) {}

	GroupInfo(const LOCALGROUP_INFO_1 p_info) :
		name(p_info.lgrpi1_name), comment(p_info.lgrpi1_comment), is_global(false) {}

	GroupInfo(JNIEnv *p_env, const jobject p_info);

	const GROUP_INFO_0& GetGroupInfo0()
	{
		if (!is_global)
			throw NTMetaDataException(L"Error at GroupInfo::GetGroupInfo0() - cannot get global group structure when group is local");
		SetGroupInfoUnion();
		return m_gi0;
	}

	const GROUP_INFO_1& GetGroupInfo1()
	{
		if (!is_global)
			throw NTMetaDataException(L"Error at GroupInfo::GetGroupInfo1() - cannot get global group structure when group is local");
		SetGroupInfoUnion();
		return m_gi1;
	}
	
	const LOCALGROUP_INFO_0& GetLocalGroupInfo0()
	{
		if (is_global)
			throw NTMetaDataException(L"Error at GroupInfo::GetLocalGroupInfo0() - cannot get local group structure when group is global");
		SetGroupInfoUnion();
		return m_lgi0;
	}

	const LOCALGROUP_INFO_1& GetLocalGroupInfo1()
	{
		if (is_global)
			throw NTMetaDataException(L"Error at GroupInfo::GetLocalGroupInfo1() - cannot get local group structure when group is global");
		SetGroupInfoUnion();
		return m_lgi1;
	}

	GroupInfo& operator =(const GroupInfo & p_info)
	{
		name = p_info.name;
		comment = p_info.comment;
		is_global = p_info.is_global;
		return *this;
	}

	virtual jobject GetJObject(JNIEnv *p_env) const;
};



// NT User Data wrapper structure
struct UserInfo
{
    Wrapper_LPWSTR	name;
    Wrapper_LPWSTR	password;
	bool			is_password_null; // To check if it came from Java as null not an empty string 
    int				password_age;
    int				priv;
    Wrapper_LPWSTR	home_dir;
    Wrapper_LPWSTR	comment;
    int				flags;
    Wrapper_LPWSTR	script_path;
    int				auth_flags;
    Wrapper_LPWSTR	full_name;
    Wrapper_LPWSTR	usr_comment;
    Wrapper_LPWSTR	parms;
    Wrapper_LPWSTR	workstations;
    int  			last_logon;
    int				last_logoff;
    int				acct_expires;
    int				max_storage;
    int				units_per_week;
    jbyte			logon_hours[LOGON_HOURS_ARRAY_LENGTH];
	bool			is_logon_hours_null; // To check if it came from Java as null 
    int				bad_pw_count;
    int				num_logons;
    Wrapper_LPWSTR	logon_server;
    int				country_code;
    int				code_page;
    int				user_id;
    int				primary_group_id;
    Wrapper_LPWSTR	profile;
    Wrapper_LPWSTR	home_dir_drive;
    int				password_expired;
	Wrapper_LPWSTR  primary_group_name;

private:
	union
	{
		USER_INFO_0 m_ui0;
		USER_INFO_3 m_ui3;
	};

public:
	UserInfo(JNIEnv *p_env, const jobject p_info);
	UserInfo(USER_INFO_3 *p_ui3, Wrapper_LPWSTR p_primary_group_name) :
		name(p_ui3->usri3_name),
		password(p_ui3->usri3_password),
		password_age(p_ui3->usri3_password_age),
		priv(p_ui3->usri3_priv),
		home_dir(p_ui3->usri3_home_dir),
		comment(p_ui3->usri3_comment),
		flags(p_ui3->usri3_flags),
		script_path(p_ui3->usri3_script_path),
		auth_flags(p_ui3->usri3_auth_flags),
		full_name(p_ui3->usri3_full_name),
		usr_comment(p_ui3->usri3_usr_comment),
		parms(p_ui3->usri3_parms),
		workstations(p_ui3->usri3_workstations),
		last_logon(p_ui3->usri3_last_logon),
		last_logoff(p_ui3->usri3_last_logoff),
		acct_expires(p_ui3->usri3_acct_expires),
		max_storage(p_ui3->usri3_max_storage),
		units_per_week(p_ui3->usri3_units_per_week),
		bad_pw_count(p_ui3->usri3_bad_pw_count),
		num_logons(p_ui3->usri3_num_logons),
		logon_server(p_ui3->usri3_logon_server),
		country_code(p_ui3->usri3_country_code),
		code_page(p_ui3->usri3_code_page),
		user_id(p_ui3->usri3_user_id),
		primary_group_id(p_ui3->usri3_primary_group_id),
		profile(p_ui3->usri3_profile),
		home_dir_drive(p_ui3->usri3_home_dir_drive),
		password_expired(p_ui3->usri3_password_expired),
		primary_group_name(p_primary_group_name),
		is_password_null(p_ui3->usri3_password?false:true),
		is_logon_hours_null(p_ui3->usri3_logon_hours?false:true)
	{	
		
		if(p_ui3->usri3_logon_hours)
		{
			for (int i=0; i < LOGON_HOURS_ARRAY_LENGTH; i++)
			{
				logon_hours[i] = p_ui3->usri3_logon_hours[i];
			}
		}
		else
		{
			for (int i=0; i < LOGON_HOURS_ARRAY_LENGTH; i++)
			{
				logon_hours[i] = 0;
			}
		}
		
	}
	UserInfo(){}


	const USER_INFO_3& GetUserInfo3()
	{
		
		m_ui3.usri3_name  = name;
		is_password_null ? m_ui3.usri3_password = NULL : m_ui3.usri3_password = password;
		m_ui3.usri3_password_age = password_age;
		m_ui3.usri3_priv = priv;
		m_ui3.usri3_home_dir = home_dir;
		m_ui3.usri3_comment = comment;
		m_ui3.usri3_flags = flags;
		m_ui3.usri3_script_path = script_path;
		m_ui3.usri3_auth_flags = auth_flags;
		m_ui3.usri3_full_name = full_name;
		m_ui3.usri3_usr_comment = usr_comment;
		m_ui3.usri3_parms = parms;
		m_ui3.usri3_workstations = workstations;
		m_ui3.usri3_last_logon = last_logon;
		m_ui3.usri3_last_logoff = last_logoff;
		m_ui3.usri3_acct_expires = acct_expires;
		m_ui3.usri3_max_storage = max_storage;
		m_ui3.usri3_units_per_week = units_per_week;
		is_logon_hours_null ? m_ui3.usri3_logon_hours = NULL : m_ui3.usri3_logon_hours = (PBYTE)&logon_hours;
		m_ui3.usri3_bad_pw_count = bad_pw_count;
		m_ui3.usri3_num_logons = num_logons;
		m_ui3.usri3_logon_server = logon_server;
		m_ui3.usri3_country_code = country_code;
		m_ui3.usri3_code_page = code_page;
		m_ui3.usri3_user_id = user_id;
		m_ui3.usri3_primary_group_id = primary_group_id;
		m_ui3.usri3_profile = profile;
		m_ui3.usri3_home_dir_drive = home_dir_drive;
		m_ui3.usri3_password_expired = password_expired;
		return m_ui3;
	}

	const USER_INFO_0& GetUserInfo0()
	{
		m_ui0.usri0_name = name;
		return m_ui0;
	}
	
	virtual jobject GetJObject(JNIEnv *p_env) const;
};





// ****************************************************************
// NTMetaData class definition
// ****************************************************************


class NTMetaData
{
private:
	Wrapper_LPWSTR m_server_name;

	const std::vector<Wrapper_LPWSTR> GetLocalGroupMembers(Wrapper_LPWSTR p_group_name, int p_members_type) const;
	void LocalGroupAddMembers(Wrapper_LPWSTR p_local_group_name, const std::vector<Wrapper_LPWSTR> &p_info) const;
	void LocalGroupDelMembers(Wrapper_LPWSTR p_local_group_name, const std::vector<Wrapper_LPWSTR> &p_info) const;
	const Wrapper_LPWSTR GetUserPrimaryGroup(DWORD p_primary_group_id) const;
	DWORD GetGroupId(Wrapper_LPWSTR p_group_name) const;
	const std::vector<GroupInfo> GetAllLocalGroupsData() const;
	const std::vector<GroupInfo> GetAllGlobalGroupsData() const;
	const std::vector<UserInfo> GetAllUsersData() const;
	const Wrapper_LPWSTR GetPureUserName(Wrapper_LPWSTR name) const;
	
public:
	NTMetaData(const std::wstring p_server_name):m_server_name(std::wstring(SERVER_NAME_PREFIX) + p_server_name){};

	bool LogOn(Wrapper_LPWSTR p_user_name, Wrapper_LPWSTR p_password) const;
	bool IsPrimaryDomainController() const;



	// ----------------------
	// Global Group Functions
	// ----------------------

	// Retrieving
	const DWORD GetGlobalGroupsNames(DWORD start_index, DWORD entries_requested, std::vector<Wrapper_LPWSTR> &names) const;
	const std::vector<Wrapper_LPWSTR> GetGlobalGroupUsers(Wrapper_LPWSTR p_group_name) const;
	const GroupInfo GlobalGroupGetInfo(Wrapper_LPWSTR p_group_name) const;
	
	// Modyfying
	void GlobalGroupAdd(GroupInfo p_info) const;
	void GlobalGroupAddUser(Wrapper_LPWSTR p_group_name, Wrapper_LPWSTR p_user_name) const;
	void GlobalGroupDel(Wrapper_LPWSTR p_name) const;
	void GlobalGroupDelUser(Wrapper_LPWSTR p_group_name, Wrapper_LPWSTR p_user_name) const;
	void GlobalGroupSetInfo(Wrapper_LPWSTR p_group_name, GroupInfo p_info) const;
	void GlobalGroupSetUsers(Wrapper_LPWSTR p_group_name,  const std::vector<Wrapper_LPWSTR> &p_info) const;



	// ---------------------
	// Local Group Functions
	// ---------------------

	// Retrieving
	const std::vector<Wrapper_LPWSTR> GetLocalGroupsNames() const;
	const std::vector<Wrapper_LPWSTR> GetLocalGroupUsers(Wrapper_LPWSTR p_group_name) const;
	const std::vector<Wrapper_LPWSTR> GetLocalGroupGlobalGroups(Wrapper_LPWSTR p_group_name) const;
	const GroupInfo LocalGroupGetInfo(Wrapper_LPWSTR p_group_name) const;

	// Modifying 
	void LocalGroupAdd(GroupInfo p_info) const;
	void LocalGroupAddUsers(Wrapper_LPWSTR p_local_group_name, const std::vector<Wrapper_LPWSTR> &p_info) const;
	void LocalGroupAddGlobalGroups(Wrapper_LPWSTR p_local_group_name, const std::vector<Wrapper_LPWSTR> &p_info) const;
	void LocalGroupDel(Wrapper_LPWSTR p_name) const;
	void LocalGroupDelUsers(Wrapper_LPWSTR p_local_group_name, const std::vector<Wrapper_LPWSTR> &p_info) const;
	void LocalGroupDelGlobalGroups(Wrapper_LPWSTR p_local_group_name, const std::vector<Wrapper_LPWSTR> &p_info) const;
	void LocalGroupSetInfo(Wrapper_LPWSTR p_name, GroupInfo p_info) const;
	void LocalGroupSetMembers(Wrapper_LPWSTR p_local_group_name, const std::vector<Wrapper_LPWSTR> &p_info) const;



	// --------------
	// User Functions
	// --------------

	// Retrieving
	const UserInfo UserGetInfo(Wrapper_LPWSTR p_user_name) const;
	const DWORD GetUsersNames(DWORD start_index, DWORD entries_requested, std::vector<Wrapper_LPWSTR> &names) const;
	const std::vector<Wrapper_LPWSTR> GetUserLocalGroups(Wrapper_LPWSTR p_user_name) const;
	const std::vector<Wrapper_LPWSTR> GetUserGlobalGroups(Wrapper_LPWSTR p_user_name) const;

	// Modyfing
	void UserAdd(UserInfo p_info) const;
	void UserChangePassword(Wrapper_LPWSTR p_user_name, Wrapper_LPWSTR p_old_psw, Wrapper_LPWSTR p_new_psw) const;
	void UserDel(Wrapper_LPWSTR p_name) const;
	void UserSetGlobalGroups(Wrapper_LPWSTR p_user_name,  const std::vector<Wrapper_LPWSTR> &p_info) const;
	void UserSetInfo(Wrapper_LPWSTR p_user_name, UserInfo p_user_info) const;
	void UserSetPrimaryGroup(Wrapper_LPWSTR p_user_name, Wrapper_LPWSTR p_group_name) const;
	const Wrapper_LPWSTR UserGetPrimaryGroup(Wrapper_LPWSTR p_user_name) const;
};



// ****************************************************************
// definition of utils not exported in the DLL (not used so far)
// ****************************************************************

const Wrapper_LPWSTR GetLocalMachineName();
const Wrapper_LPWSTR GetDomainControllerComputerName();
void GetDomainAndUserName(Wrapper_LPWSTR &p_domain_name,Wrapper_LPWSTR &p_user_name);
const Wrapper_LPWSTR GetDomainName();


#endif
