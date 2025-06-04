import 'dart:convert';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:http/http.dart' as http;
import 'package:flutter/material.dart';

const String apiBaseUrl = 'http://192.168.0.183:5182/api';

class AdminApiService {
  Future<String?> _getAuthToken() async {
    return await FirebaseAuth.instance.currentUser?.getIdToken();
  }

  Future<List<dynamic>> fetchConsultants(BuildContext context) async {
    try {
      final token = await _getAuthToken();
      final response = await http.get(
        Uri.parse('$apiBaseUrl/Consultant/get-all-consultants'),
        headers: {
          'Authorization': 'Bearer $token',
          'Content-Type': 'application/json',
        },
      );

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        debugPrint('Consultants: $data');
        return data;
      } else {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Error loading consultants: ${response.statusCode}'),
            backgroundColor: Colors.red,
          ),
        );
        return [];
      }
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error: $e'), backgroundColor: Colors.red),
      );
      return [];
    }
  }

  Future<List<dynamic>> fetchUsers(BuildContext context) async {
    try {
      final token = await _getAuthToken();
      final response = await http.get(
        Uri.parse('$apiBaseUrl/User/get-all-users'),
        headers: {
          'Authorization': 'Bearer $token',
          'Content-Type': 'application/json',
        },
      );

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        debugPrint('Users: $data');
        return data;
      } else {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Error loading users: ${response.statusCode}'),
            backgroundColor: Colors.red,
          ),
        );
        return [];
      }
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error: $e'), backgroundColor: Colors.red),
      );
      return [];
    }
  }

  Future<Map<String, dynamic>?> fetchStatistics(BuildContext context) async {
    try {
      final token = await _getAuthToken();
      final response = await http.get(
        Uri.parse('$apiBaseUrl/Admin/get-statistics'),
        headers: {
          'Authorization': 'Bearer $token',
          'Content-Type': 'application/json',
        },
      );

      if (response.statusCode == 200) {
        return jsonDecode(response.body);
      } else {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Error loading statistics: ${response.statusCode}'),
            backgroundColor: Colors.red,
          ),
        );
        return null;
      }
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error: $e'), backgroundColor: Colors.red),
      );
      return null;
    }
  }

  Future<bool> deleteUser(String userUid, BuildContext context) async {
    try {
      final token = await _getAuthToken();
      final response = await http.delete(
        Uri.parse('$apiBaseUrl/Admin/remove-user/$userUid'),
        headers: {
          'Authorization': 'Bearer $token',
          'Content-Type': 'application/json',
        },
      );

      if (response.statusCode == 200) {
        return true;
      } else {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Error deleting user: ${response.statusCode}'),
            backgroundColor: Colors.red,
          ),
        );
        return false;
      }
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error: $e'), backgroundColor: Colors.red),
      );
      return false;
    }
  }

  Future<bool> deleteConsultant(
    String consultantUid,
    BuildContext context,
  ) async {
    try {
      final token = await _getAuthToken();
      final response = await http.delete(
        Uri.parse('$apiBaseUrl/Admin/remove-consultant/$consultantUid'),
        headers: {
          'Authorization': 'Bearer $token',
          'Content-Type': 'application/json',
        },
      );

      if (response.statusCode == 200) {
        return true;
      } else {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Error deleting consultant: ${response.statusCode}'),
            backgroundColor: Colors.red,
          ),
        );
        return false;
      }
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error: $e'), backgroundColor: Colors.red),
      );
      return false;
    }
  }

  Future<bool> updateUserProfile(
    String userUid,
    Map<String, dynamic> updates,
    BuildContext context,
  ) async {
    try {
      final token = await _getAuthToken();
      final response = await http.patch(
        Uri.parse('$apiBaseUrl/Admin/update-user-profile/$userUid'),
        headers: {
          'Authorization': 'Bearer $token',
          'Content-Type': 'application/json',
        },
        body: jsonEncode(updates),
      );

      if (response.statusCode == 200) {
        return true;
      } else {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Error updating user: ${response.statusCode}'),
            backgroundColor: Colors.red,
          ),
        );
        return false;
      }
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error: $e'), backgroundColor: Colors.red),
      );
      return false;
    }
  }

  Future<bool> updateConsultantProfile(
    String consultantUid,
    Map<String, dynamic> updates,
    BuildContext context,
  ) async {
    try {
      final token = await _getAuthToken();
      final response = await http.patch(
        Uri.parse('$apiBaseUrl/Admin/update-consultant-profile/$consultantUid'),
        headers: {
          'Authorization': 'Bearer $token',
          'Content-Type': 'application/json',
        },
        body: jsonEncode(updates),
      );

      if (response.statusCode == 200) {
        return true;
      } else {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Error updating consultant: ${response.statusCode}'),
            backgroundColor: Colors.red,
          ),
        );
        return false;
      }
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error: $e'), backgroundColor: Colors.red),
      );
      return false;
    }
  }

  Future<bool> createDatabaseBackup(BuildContext context) async {
    try {
      final token = await _getAuthToken();
      final response = await http.post(
        Uri.parse('$apiBaseUrl/Database/backup'),
        headers: {
          'Authorization': 'Bearer $token',
          'Content-Type': 'application/json',
        },
      );

      if (response.statusCode == 200) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Database backup created successfully'),
            backgroundColor: Colors.green,
          ),
        );
        return true;
      } else {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Error creating backup: ${response.statusCode}'),
            backgroundColor: Colors.red,
          ),
        );
        return false;
      }
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error: $e'), backgroundColor: Colors.red),
      );
      return false;
    }
  }
}
