import 'dart:async';
import 'package:flutter/material.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:nutri_track_web_admin/screens/auth_screen.dart';
import 'package:nutri_track_web_admin/services.dart';
import 'package:nutri_track_web_admin/widgets.dart';

const Color primaryColor = Color(0xFF2F4F4F);
const Color secondaryColor = Color(0xFF64A79B);
const Color accentColor = Colors.blueAccent;
const Color textColorLight = Colors.white;

class AdminMainScreen extends StatefulWidget {
  const AdminMainScreen({Key? key}) : super(key: key);

  @override
  State<AdminMainScreen> createState() => _AdminMainScreenState();
}

class _AdminMainScreenState extends State<AdminMainScreen>
    with SingleTickerProviderStateMixin {
  late TabController _tabController;
  List<dynamic> consultants = [];
  List<dynamic> users = [];
  List<dynamic> filteredConsultants = [];
  List<dynamic> filteredUsers = [];
  bool isLoadingConsultants = true;
  bool isLoadingUsers = true;
  bool isCreatingBackup = false;
  final TextEditingController _searchController = TextEditingController();
  final AdminApiService _apiService = AdminApiService();

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 2, vsync: this);
    fetchConsultants();
    fetchUsers();
    _searchController.addListener(_filterLists);
  }

  @override
  void dispose() {
    _tabController.dispose();
    _searchController.dispose();
    super.dispose();
  }

  void _filterLists() {
    final query = _searchController.text.toLowerCase();
    setState(() {
      filteredConsultants =
          consultants
              .where(
                (consultant) =>
                    consultant['nickname']?.toLowerCase()?.contains(query) ??
                    false,
              )
              .toList();
      filteredUsers =
          users
              .where(
                (user) =>
                    user['nickname']?.toLowerCase()?.contains(query) ?? false,
              )
              .toList();
    });
  }

  Future<void> fetchConsultants() async {
    setState(() {
      isLoadingConsultants = true;
    });
    final data = await _apiService.fetchConsultants(context);
    setState(() {
      consultants = data.map((item) => _castToMap(item)).toList();
      filteredConsultants = List.from(consultants); // Створюємо копію
      isLoadingConsultants = false;
    });
  }

  Future<void> fetchUsers() async {
    setState(() {
      isLoadingUsers = true;
    });
    final data = await _apiService.fetchUsers(context);
    setState(() {
      users = data.map((item) => _castToMap(item)).toList();
      filteredUsers = List.from(users); // Створюємо копію
      isLoadingUsers = false;
    });
  }

  void showStatisticsDialog() async {
    setState(() {
      isLoadingUsers = true;
    });

    final stats = await _apiService.fetchStatistics(context);

    setState(() {
      isLoadingUsers = false;
    });

    AdminWidgets.showStatisticsDialog(
      context: context,
      stats: stats,
      isMounted: mounted,
    );
  }

  Map<String, dynamic> _castToMap(dynamic item) {
    if (item is Map) {
      return item.map((key, value) => MapEntry(key.toString(), value));
    }
    return {};
  }

  @override
  Widget build(BuildContext context) {
    final isWideScreen = MediaQuery.of(context).size.width > 600;

    return Scaffold(
      backgroundColor: secondaryColor.withOpacity(0.3),
      appBar: AppBar(
        title: Text(
          'NutriTrack Admin Panel',
          style: TextStyle(fontWeight: FontWeight.w600, color: textColorLight),
        ),
        backgroundColor: primaryColor,
        elevation: 2,
        actions: [
          IconButton(
            icon: Icon(Icons.bar_chart, color: textColorLight),
            tooltip: 'View Statistics',
            onPressed: showStatisticsDialog,
          ),
          IconButton(
            icon:
                isCreatingBackup
                    ? SizedBox(
                      width: 24,
                      height: 24,
                      child: CircularProgressIndicator(
                        color: textColorLight,
                        strokeWidth: 2,
                      ),
                    )
                    : Icon(Icons.backup, color: textColorLight),
            tooltip: 'Create Database Backup',
            onPressed:
                isCreatingBackup
                    ? null
                    : () async {
                      final confirmed =
                          await AdminWidgets.showBackupConfirmationDialog(
                            context,
                          );
                      if (confirmed) {
                        setState(() {
                          isCreatingBackup = true;
                        });
                        final success = await _apiService.createDatabaseBackup(
                          context,
                        );
                        setState(() {
                          isCreatingBackup = false;
                        });
                      }
                    },
          ),
          IconButton(
            icon: Icon(Icons.refresh, color: textColorLight),
            tooltip: 'Refresh Data',
            onPressed: () {
              fetchConsultants();
              fetchUsers();
              ScaffoldMessenger.of(context).showSnackBar(
                SnackBar(
                  content: Text('Refreshing data...'),
                  backgroundColor: accentColor,
                ),
              );
            },
          ),
          IconButton(
            icon: Icon(Icons.logout, color: textColorLight),
            tooltip: 'Logout',
            onPressed: () async {
              await FirebaseAuth.instance.signOut();
              Navigator.pushReplacement(
                context,
                MaterialPageRoute(builder: (_) => AdminAuthScreen()),
              );
            },
          ),
        ],
        bottom:
            isWideScreen
                ? null
                : TabBar(
                  controller: _tabController,
                  indicatorColor: accentColor,
                  labelColor: textColorLight,
                  unselectedLabelColor: Colors.white70,
                  labelStyle: TextStyle(fontWeight: FontWeight.w600),
                  tabs: [Tab(text: 'Consultants'), Tab(text: 'Users')],
                ),
      ),
      body: Row(
        children: [
          if (isWideScreen)
            NavigationRail(
              selectedIndex: _tabController.index,
              onDestinationSelected: (index) {
                setState(() {
                  _tabController.animateTo(index);
                });
              },
              labelType: NavigationRailLabelType.all,
              backgroundColor: primaryColor,
              selectedIconTheme: IconThemeData(color: textColorLight),
              unselectedIconTheme: IconThemeData(color: Colors.white70),
              selectedLabelTextStyle: TextStyle(
                color: textColorLight,
                fontWeight: FontWeight.w600,
              ),
              unselectedLabelTextStyle: TextStyle(color: Colors.white70),
              destinations: [
                NavigationRailDestination(
                  icon: Icon(Icons.person_outline),
                  selectedIcon: Icon(Icons.person),
                  label: Text('Consultants'),
                ),
                NavigationRailDestination(
                  icon: Icon(Icons.people_outline),
                  selectedIcon: Icon(Icons.people),
                  label: Text('Users'),
                ),
              ],
            ),
          Expanded(
            child: Container(
              color: secondaryColor.withOpacity(0.3),
              child: TabBarView(
                controller: _tabController,
                children: [
                  Column(
                    children: [
                      AdminWidgets.buildSearchBar(_searchController),
                      Expanded(
                        child:
                            isLoadingConsultants
                                ? Center(
                                  child: CircularProgressIndicator(
                                    color: textColorLight,
                                  ),
                                )
                                : RefreshIndicator(
                                  onRefresh: fetchConsultants,
                                  color: primaryColor,
                                  child:
                                      filteredConsultants.isEmpty
                                          ? Center(
                                            child: Text(
                                              _searchController.text.isEmpty
                                                  ? 'No consultants'
                                                  : 'No consultants found',
                                              style: TextStyle(
                                                color: textColorLight,
                                                fontSize: 16,
                                              ),
                                            ),
                                          )
                                          : ListView.separated(
                                            padding: EdgeInsets.all(16),
                                            itemCount:
                                                filteredConsultants.length,
                                            separatorBuilder:
                                                (_, __) => SizedBox(height: 12),
                                            itemBuilder: (_, itemIndex) {
                                              final consultant =
                                                  filteredConsultants[itemIndex];
                                              final consultantUid =
                                                  consultant['consultant_uid']
                                                      as String?;
                                              return AdminWidgets.buildConsultantItem(
                                                consultant: consultant,
                                                onEdit: () async {
                                                  if (consultantUid == null) {
                                                    debugPrint(
                                                      'Error: Consultant UID is null for consultant: $consultant',
                                                    );
                                                    ScaffoldMessenger.of(
                                                      context,
                                                    ).showSnackBar(
                                                      SnackBar(
                                                        content: Text(
                                                          'Cannot edit consultant: Missing UID',
                                                        ),
                                                        backgroundColor:
                                                            Colors.red,
                                                      ),
                                                    );
                                                    return;
                                                  }
                                                  debugPrint(
                                                    'Editing consultant with experience_years: ${consultant['experience_years']}',
                                                  );
                                                  final saved = await AdminWidgets.showEditDialog(
                                                    context: context,
                                                    type: 'Consultant',
                                                    uid: consultantUid,
                                                    nickname:
                                                        consultant['nickname']
                                                            as String?,
                                                    profilePicture:
                                                        consultant['profile_picture']
                                                            as String?,
                                                    profileDescription:
                                                        consultant['profile_description']
                                                            as String?,
                                                    experienceYears:
                                                        consultant['experience_years']
                                                            ?.toString() ??
                                                        '',
                                                    onSave: (updates) async {
                                                      final castedUpdates =
                                                          _castToMap(updates);
                                                      final success =
                                                          await _apiService
                                                              .updateConsultantProfile(
                                                                consultantUid,
                                                                castedUpdates,
                                                                context,
                                                              );
                                                      if (success) {
                                                        setState(() {
                                                          final originalIndex =
                                                              consultants.indexWhere(
                                                                (c) =>
                                                                    c['consultant_uid'] ==
                                                                    consultantUid,
                                                              );
                                                          if (originalIndex !=
                                                              -1) {
                                                            consultants[originalIndex] = {
                                                              ..._castToMap(
                                                                consultants[originalIndex],
                                                              ),
                                                              ...castedUpdates,
                                                            };
                                                          }
                                                          _filterLists();
                                                        });
                                                        ScaffoldMessenger.of(
                                                          context,
                                                        ).showSnackBar(
                                                          SnackBar(
                                                            content: Text(
                                                              'Consultant updated successfully',
                                                            ),
                                                            backgroundColor:
                                                                Colors.green,
                                                          ),
                                                        );
                                                      }
                                                    },
                                                  );
                                                  if (!saved) {
                                                    ScaffoldMessenger.of(
                                                      context,
                                                    ).showSnackBar(
                                                      SnackBar(
                                                        content: Text(
                                                          'Consultant update cancelled',
                                                        ),
                                                        backgroundColor:
                                                            Colors.grey,
                                                      ),
                                                    );
                                                  }
                                                },
                                                onDelete: () async {
                                                  if (consultantUid == null) {
                                                    debugPrint(
                                                      'Error: Consultant UID is null for consultant: $consultant',
                                                    );
                                                    ScaffoldMessenger.of(
                                                      context,
                                                    ).showSnackBar(
                                                      SnackBar(
                                                        content: Text(
                                                          'Cannot delete consultant: Missing UID',
                                                        ),
                                                        backgroundColor:
                                                            Colors.red,
                                                      ),
                                                    );
                                                    return;
                                                  }
                                                  final confirmed =
                                                      await AdminWidgets.showDeleteConfirmationDialog(
                                                        context,
                                                        consultant['nickname'] ??
                                                            'Consultant',
                                                        'Consultant',
                                                      );
                                                  if (confirmed) {
                                                    final success =
                                                        await _apiService
                                                            .deleteConsultant(
                                                              consultantUid,
                                                              context,
                                                            );
                                                    if (success) {
                                                      setState(() {
                                                        consultants.removeWhere(
                                                          (c) =>
                                                              c['consultant_uid'] ==
                                                              consultantUid,
                                                        );
                                                        _filterLists();
                                                      });
                                                      ScaffoldMessenger.of(
                                                        context,
                                                      ).showSnackBar(
                                                        SnackBar(
                                                          content: Text(
                                                            'Consultant deleted successfully',
                                                          ),
                                                          backgroundColor:
                                                              Colors.green,
                                                        ),
                                                      );
                                                    }
                                                  }
                                                },
                                              );
                                            },
                                          ),
                                ),
                      ),
                    ],
                  ),
                  Column(
                    children: [
                      AdminWidgets.buildSearchBar(_searchController),
                      Expanded(
                        child:
                            isLoadingUsers
                                ? Center(
                                  child: CircularProgressIndicator(
                                    color: textColorLight,
                                  ),
                                )
                                : RefreshIndicator(
                                  onRefresh: fetchUsers,
                                  color: primaryColor,
                                  child:
                                      filteredUsers.isEmpty
                                          ? Center(
                                            child: Text(
                                              _searchController.text.isEmpty
                                                  ? 'No users'
                                                  : 'No users found',
                                              style: TextStyle(
                                                color: textColorLight,
                                                fontSize: 16,
                                              ),
                                            ),
                                          )
                                          : ListView.separated(
                                            padding: EdgeInsets.all(16),
                                            itemCount: filteredUsers.length,
                                            separatorBuilder:
                                                (_, __) => SizedBox(height: 12),
                                            itemBuilder: (_, itemIndex) {
                                              final user =
                                                  filteredUsers[itemIndex];
                                              final userUid =
                                                  user['user_uid'] as String?;
                                              return AdminWidgets.buildUserItem(
                                                user: user,
                                                onEdit: () async {
                                                  if (userUid == null) {
                                                    debugPrint(
                                                      'Error: User UID is null for user: $user',
                                                    );
                                                    ScaffoldMessenger.of(
                                                      context,
                                                    ).showSnackBar(
                                                      SnackBar(
                                                        content: Text(
                                                          'Cannot edit user: Missing UID',
                                                        ),
                                                        backgroundColor:
                                                            Colors.red,
                                                      ),
                                                    );
                                                    return;
                                                  }
                                                  final saved = await AdminWidgets.showEditDialog(
                                                    context: context,
                                                    type: 'User',
                                                    uid: userUid,
                                                    nickname:
                                                        user['nickname']
                                                            as String?,
                                                    profilePicture:
                                                        user['profile_picture']
                                                            as String?,
                                                    profileDescription:
                                                        user['profile_description']
                                                            as String?,
                                                    onSave: (updates) async {
                                                      final castedUpdates =
                                                          _castToMap(updates);
                                                      final success =
                                                          await _apiService
                                                              .updateUserProfile(
                                                                userUid,
                                                                castedUpdates,
                                                                context,
                                                              );
                                                      if (success) {
                                                        setState(() {
                                                          final originalIndex =
                                                              users.indexWhere(
                                                                (u) =>
                                                                    u['user_uid'] ==
                                                                    userUid,
                                                              );
                                                          if (originalIndex !=
                                                              -1) {
                                                            users[originalIndex] = {
                                                              ..._castToMap(
                                                                users[originalIndex],
                                                              ),
                                                              ...castedUpdates,
                                                            };
                                                          }
                                                          _filterLists();
                                                        });
                                                        ScaffoldMessenger.of(
                                                          context,
                                                        ).showSnackBar(
                                                          SnackBar(
                                                            content: Text(
                                                              'User updated successfully',
                                                            ),
                                                            backgroundColor:
                                                                Colors.green,
                                                          ),
                                                        );
                                                      }
                                                    },
                                                  );
                                                  if (!saved) {
                                                    ScaffoldMessenger.of(
                                                      context,
                                                    ).showSnackBar(
                                                      SnackBar(
                                                        content: Text(
                                                          'User update cancelled',
                                                        ),
                                                        backgroundColor:
                                                            Colors.grey,
                                                      ),
                                                    );
                                                  }
                                                },
                                                onDelete: () async {
                                                  if (userUid == null) {
                                                    debugPrint(
                                                      'Error: User UID is null for user: $user',
                                                    );
                                                    ScaffoldMessenger.of(
                                                      context,
                                                    ).showSnackBar(
                                                      SnackBar(
                                                        content: Text(
                                                          'Cannot delete user: Missing UID',
                                                        ),
                                                        backgroundColor:
                                                            Colors.red,
                                                      ),
                                                    );
                                                    return;
                                                  }
                                                  final confirmed =
                                                      await AdminWidgets.showDeleteConfirmationDialog(
                                                        context,
                                                        user['nickname'] ??
                                                            'User',
                                                        'User',
                                                      );
                                                  if (confirmed) {
                                                    final success =
                                                        await _apiService
                                                            .deleteUser(
                                                              userUid,
                                                              context,
                                                            );
                                                    if (success) {
                                                      setState(() {
                                                        users.removeWhere(
                                                          (u) =>
                                                              u['user_uid'] ==
                                                              userUid,
                                                        );
                                                        _filterLists();
                                                      });
                                                      ScaffoldMessenger.of(
                                                        context,
                                                      ).showSnackBar(
                                                        SnackBar(
                                                          content: Text(
                                                            'User deleted successfully',
                                                          ),
                                                          backgroundColor:
                                                              Colors.green,
                                                        ),
                                                      );
                                                    }
                                                  }
                                                },
                                              );
                                            },
                                          ),
                                ),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}
