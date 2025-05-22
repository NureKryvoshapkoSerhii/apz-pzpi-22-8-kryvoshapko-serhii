import 'package:flutter/material.dart';

const Color primaryColor = Color(0xFF2F4F4F);
const Color secondaryColor = Color(0xFF64A79B);
const Color accentColor = Colors.blueAccent;
const Color textColorLight = Colors.white;
const Color textColorDark = Colors.black87;
const Color cardColor = Color(0xFF2F4F4F);

class AdminWidgets {
  static String? getOptimizedImageUrl(String? url) {
    if (url == null || url.isEmpty) {
      return null;
    }

    if (url.contains('firebasestorage.googleapis.com')) {
      String baseUrl = url.split('?').first;
      if (baseUrl.contains('/o/')) {
        return '$baseUrl?alt=media';
      }
    }

    return url;
  }

  static Widget buildProfileImage(String? imageUrl) {
    final optimizedUrl = getOptimizedImageUrl(imageUrl);

    return Container(
      width: 60,
      height: 60,
      decoration: BoxDecoration(
        shape: BoxShape.circle,
        color: Colors.grey[300],
        boxShadow: [
          BoxShadow(color: Colors.black12, blurRadius: 4, offset: Offset(0, 2)),
        ],
      ),
      clipBehavior: Clip.antiAlias,
      child:
          optimizedUrl == null
              ? Center(child: Icon(Icons.person, size: 30, color: primaryColor))
              : Image.network(
                optimizedUrl,
                width: 60,
                height: 60,
                fit: BoxFit.cover,
                errorBuilder: (context, error, stackTrace) {
                  debugPrint('Error loading image: $optimizedUrl - $error');
                  return Center(
                    child: Icon(Icons.person, size: 30, color: primaryColor),
                  );
                },
                loadingBuilder: (context, child, loadingProgress) {
                  if (loadingProgress == null) return child;
                  return Center(
                    child: CircularProgressIndicator(
                      value:
                          loadingProgress.expectedTotalBytes != null
                              ? loadingProgress.cumulativeBytesLoaded /
                                  loadingProgress.expectedTotalBytes!
                              : null,
                      color: primaryColor,
                      strokeWidth: 2,
                    ),
                  );
                },
              ),
    );
  }

  static Widget buildSearchBar(TextEditingController controller) {
    return Padding(
      padding: EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      child: TextField(
        controller: controller,
        decoration: InputDecoration(
          hintText: 'Search by nickname...',
          hintStyle: TextStyle(color: textColorLight.withOpacity(0.7)),
          prefixIcon: Icon(Icons.search, color: textColorLight),
          filled: true,
          fillColor: primaryColor.withOpacity(0.3),
          border: OutlineInputBorder(
            borderRadius: BorderRadius.circular(12),
            borderSide: BorderSide.none,
          ),
          focusedBorder: OutlineInputBorder(
            borderRadius: BorderRadius.circular(12),
            borderSide: BorderSide(color: accentColor),
          ),
        ),
        style: TextStyle(color: textColorLight),
      ),
    );
  }

  static Future<bool> showDeleteConfirmationDialog(
    BuildContext context,
    String name,
    String type,
  ) async {
    TextEditingController controller = TextEditingController();
    bool confirmed = false;

    await showDialog(
      context: context,
      builder:
          (context) => AlertDialog(
            backgroundColor: cardColor,
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(12),
            ),
            title: Text(
              'Confirm Deletion',
              style: TextStyle(
                color: textColorLight,
                fontWeight: FontWeight.w600,
              ),
            ),
            content: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'To delete $type "${name.isEmpty ? 'Unknown' : name}", type "DELETE" below:',
                  style: TextStyle(color: textColorLight),
                ),
                TextField(
                  controller: controller,
                  decoration: InputDecoration(
                    hintText: 'Type DELETE',
                    hintStyle: TextStyle(color: Colors.white70),
                    enabledBorder: UnderlineInputBorder(
                      borderSide: BorderSide(color: textColorLight),
                    ),
                    focusedBorder: UnderlineInputBorder(
                      borderSide: BorderSide(color: accentColor),
                    ),
                  ),
                  style: TextStyle(color: textColorLight),
                ),
              ],
            ),
            actions: [
              TextButton(
                onPressed: () => Navigator.pop(context),
                child: Text('Cancel', style: TextStyle(color: accentColor)),
              ),
              TextButton(
                onPressed: () {
                  if (controller.text == 'DELETE') {
                    confirmed = true;
                    Navigator.pop(context);
                  } else {
                    ScaffoldMessenger.of(context).showSnackBar(
                      SnackBar(
                        content: Text('Please type "DELETE" to confirm'),
                        backgroundColor: Colors.red,
                      ),
                    );
                  }
                },
                child: Text('Delete', style: TextStyle(color: Colors.red)),
              ),
            ],
          ),
    );

    return confirmed;
  }

  static Future<bool> showBackupConfirmationDialog(BuildContext context) async {
    bool confirmed = false;

    await showDialog(
      context: context,
      builder:
          (context) => AlertDialog(
            backgroundColor: cardColor,
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(12),
            ),
            title: Text(
              'Create Database Backup',
              style: TextStyle(
                color: textColorLight,
                fontWeight: FontWeight.w600,
              ),
            ),
            content: Text(
              'Are you sure you want to create a database backup? This process may take some time.',
              style: TextStyle(color: textColorLight),
            ),
            actions: [
              TextButton(
                onPressed: () => Navigator.pop(context),
                child: Text('Cancel', style: TextStyle(color: accentColor)),
              ),
              TextButton(
                onPressed: () {
                  confirmed = true;
                  Navigator.pop(context);
                },
                child: Text(
                  'Create Backup',
                  style: TextStyle(color: accentColor),
                ),
              ),
            ],
          ),
    );

    return confirmed;
  }

  static Future<bool> showEditDialog({
    required BuildContext context,
    required String type,
    required String uid,
    String? nickname,
    String? profilePicture,
    String? profileDescription,
    String? experienceYears,
    required Function(Map<String, dynamic>) onSave,
  }) async {
    final nicknameController = TextEditingController(text: nickname ?? '');
    final profilePictureController = TextEditingController(
      text: profilePicture ?? '',
    );
    final profileDescriptionController = TextEditingController(
      text: profileDescription ?? '',
    );
    final experienceYearsController = TextEditingController(
      text: experienceYears ?? '',
    );

    bool saved = false;

    await showDialog(
      context: context,
      builder:
          (context) => AlertDialog(
            backgroundColor: cardColor,
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(12),
            ),
            title: Text(
              'Edit $type Profile',
              style: TextStyle(
                color: textColorLight,
                fontWeight: FontWeight.w600,
              ),
            ),
            content: SingleChildScrollView(
              child: Column(
                mainAxisSize: MainAxisSize.min,
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  TextField(
                    controller: nicknameController,
                    decoration: InputDecoration(
                      labelText: 'Nickname',
                      labelStyle: TextStyle(color: textColorLight),
                      filled: true,
                      fillColor: secondaryColor.withOpacity(0.1),
                      border: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(8),
                        borderSide: BorderSide.none,
                      ),
                      focusedBorder: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(8),
                        borderSide: BorderSide(color: accentColor),
                      ),
                    ),
                    style: TextStyle(color: textColorLight),
                  ),
                  SizedBox(height: 12),
                  TextField(
                    controller: profilePictureController,
                    decoration: InputDecoration(
                      labelText: 'Profile Picture URL',
                      labelStyle: TextStyle(color: textColorLight),
                      filled: true,
                      fillColor: secondaryColor.withOpacity(0.1),
                      border: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(8),
                        borderSide: BorderSide.none,
                      ),
                      focusedBorder: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(8),
                        borderSide: BorderSide(color: accentColor),
                      ),
                    ),
                    style: TextStyle(color: textColorLight),
                  ),
                  SizedBox(height: 12),
                  TextField(
                    controller: profileDescriptionController,
                    decoration: InputDecoration(
                      labelText: 'Profile Description',
                      labelStyle: TextStyle(color: textColorLight),
                      filled: true,
                      fillColor: secondaryColor.withOpacity(0.1),
                      border: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(8),
                        borderSide: BorderSide.none,
                      ),
                      focusedBorder: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(8),
                        borderSide: BorderSide(color: accentColor),
                      ),
                    ),
                    style: TextStyle(color: textColorLight),
                    maxLines: 3,
                  ),
                  if (type == 'Consultant') ...[
                    SizedBox(height: 12),
                    TextField(
                      controller: experienceYearsController,
                      decoration: InputDecoration(
                        labelText: 'Experience Years',
                        labelStyle: TextStyle(color: textColorLight),
                        filled: true,
                        fillColor: secondaryColor.withOpacity(0.1),
                        border: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(8),
                          borderSide: BorderSide.none,
                        ),
                        focusedBorder: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(8),
                          borderSide: BorderSide(color: accentColor),
                        ),
                      ),
                      style: TextStyle(color: textColorLight),
                      keyboardType: TextInputType.number,
                    ),
                  ],
                ],
              ),
            ),
            actions: [
              TextButton(
                onPressed: () => Navigator.pop(context),
                child: Text('Cancel', style: TextStyle(color: accentColor)),
              ),
              TextButton(
                onPressed: () {
                  final updates = <String, dynamic>{
                    'nickname': nicknameController.text,
                    'profilePicture': profilePictureController.text,
                    'profileDescription': profileDescriptionController.text,
                  };
                  if (type == 'Consultant') {
                    final experienceYears = int.tryParse(
                      experienceYearsController.text,
                    );
                    if (experienceYears == null &&
                        experienceYearsController.text.isNotEmpty) {
                      ScaffoldMessenger.of(context).showSnackBar(
                        SnackBar(
                          content: Text(
                            'Please enter a valid number for experience years',
                          ),
                          backgroundColor: Colors.red,
                        ),
                      );
                      return;
                    }
                    updates['experienceYears'] = experienceYears;
                    debugPrint(
                      'Updating consultant with experienceYears: ${updates['experienceYears']}',
                    );
                  }
                  onSave(updates);
                  saved = true;
                  Navigator.pop(context);
                },
                child: Text('Save', style: TextStyle(color: accentColor)),
              ),
            ],
          ),
    );

    return saved;
  }

  static void showStatisticsDialog({
    required BuildContext context,
    required Map<String, dynamic>? stats,
    required bool isMounted,
  }) {
    if (stats != null && isMounted) {
      showDialog(
        context: context,
        builder:
            (context) => AlertDialog(
              backgroundColor: cardColor,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(12),
              ),
              title: Text(
                'NutriTrack Statistics',
                style: TextStyle(
                  color: textColorLight,
                  fontWeight: FontWeight.w600,
                ),
              ),
              content: Column(
                mainAxisSize: MainAxisSize.min,
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Total Users: ${stats['totalUsers'] ?? 0}',
                    style: TextStyle(color: textColorLight),
                  ),
                  SizedBox(height: 8),
                  Text(
                    'Total Consultants: ${stats['totalConsultants'] ?? 0}',
                    style: TextStyle(color: textColorLight),
                  ),
                  SizedBox(height: 8),
                  Text(
                    'Total Count: ${(stats['totalUsers'] as int? ?? 0) + (stats['totalConsultants'] as int? ?? 0)}',
                    style: TextStyle(color: textColorLight),
                  ),
                ],
              ),
              actions: [
                TextButton(
                  onPressed: () => Navigator.pop(context),
                  child: Text('Close', style: TextStyle(color: accentColor)),
                ),
              ],
            ),
      );
    }
  }

  static Widget buildUserItem({
    required Map<String, dynamic> user,
    required Function() onEdit,
    required Function() onDelete,
  }) {
    return Card(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      elevation: 4,
      color: cardColor,
      child: Padding(
        padding: EdgeInsets.all(16),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            buildProfileImage(user['profile_picture']),
            SizedBox(width: 16),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    user['nickname'] ?? 'No nickname',
                    style: TextStyle(
                      fontWeight: FontWeight.w600,
                      color: textColorLight,
                      fontSize: 16,
                    ),
                  ),
                  SizedBox(height: 8),
                  Text(
                    'Created: ${user['created_at']?.substring(0, 10) ?? '-'}',
                    style: TextStyle(color: textColorLight.withOpacity(0.8)),
                  ),
                  Text(
                    'Last Login: ${user['last_login']?.substring(0, 10) ?? '-'}',
                    style: TextStyle(color: textColorLight.withOpacity(0.8)),
                  ),
                ],
              ),
            ),
            Row(
              children: [
                IconButton(
                  icon: Icon(Icons.edit, color: accentColor),
                  onPressed: onEdit,
                ),
                IconButton(
                  icon: Icon(Icons.delete, color: Colors.red),
                  onPressed: onDelete,
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  static Widget buildConsultantItem({
    required Map<String, dynamic> consultant,
    required Function() onEdit,
    required Function() onDelete,
  }) {
    return Card(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      elevation: 4,
      color: cardColor,
      child: Padding(
        padding: EdgeInsets.all(16),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            buildProfileImage(consultant['profile_picture']),
            SizedBox(width: 16),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    consultant['nickname'] ?? 'No nickname',
                    style: TextStyle(
                      fontWeight: FontWeight.w600,
                      color: textColorLight,
                      fontSize: 16,
                    ),
                  ),
                  SizedBox(height: 8),
                  Text(
                    'Experience: ${consultant['experience_years'] ?? '-'} years',
                    style: TextStyle(color: textColorLight.withOpacity(0.8)),
                  ),
                  Text(
                    'Created: ${consultant['created_at']?.substring(0, 10) ?? '-'}',
                    style: TextStyle(color: textColorLight.withOpacity(0.8)),
                  ),
                  Text(
                    'Last Login: ${consultant['last_login']?.substring(0, 10) ?? '-'}',
                    style: TextStyle(color: textColorLight.withOpacity(0.8)),
                  ),
                ],
              ),
            ),
            Row(
              children: [
                IconButton(
                  icon: Icon(Icons.edit, color: accentColor),
                  onPressed: onEdit,
                ),
                IconButton(
                  icon: Icon(Icons.delete, color: Colors.red),
                  onPressed: onDelete,
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
