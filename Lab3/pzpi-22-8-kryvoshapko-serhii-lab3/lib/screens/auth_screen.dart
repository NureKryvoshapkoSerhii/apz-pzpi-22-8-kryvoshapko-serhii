import 'package:flutter/material.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:http/http.dart' as http;
import 'package:nutri_track_web_admin/screens/AdminMainScreen.dart';
import 'dart:convert';

const Color primaryColor = Color(0xFF2F4F4F);
const Color secondaryColor = Color(0xFF64A79B);
const Color accentColor = Colors.blueAccent;
const Color textColorLight = Colors.white;
const Color textColorDark = Colors.black87;
const Color cardColor = Color(0xFF2F4F4F);

const String apiBaseUrl = 'http://192.168.0.183:5182/api';

class AdminAuthScreen extends StatefulWidget {
  const AdminAuthScreen({Key? key}) : super(key: key);

  @override
  State<AdminAuthScreen> createState() => _AdminAuthScreenState();
}

class _AdminAuthScreenState extends State<AdminAuthScreen> {
  final FirebaseAuth _auth = FirebaseAuth.instance;
  bool isRegistering = false;
  String? _idToken;
  String? _email;
  final _nameController = TextEditingController();
  final _phoneController = TextEditingController();
  final _secretCodeController = TextEditingController();
  bool _isLoading = false;
  String? _errorMessage;

  static const String _adminSecretCode = '1599';

  Future<void> handleGoogleSignIn() async {
    setState(() {
      _isLoading = true;
      _errorMessage = null;
      debugPrint(
        'handleGoogleSignIn: Starting Google Sign-In, _isLoading = true',
      );
    });

    try {
      GoogleAuthProvider googleProvider = GoogleAuthProvider();
      UserCredential userCredential = await _auth.signInWithPopup(
        googleProvider,
      );
      final idToken = await userCredential.user?.getIdToken();
      final email = userCredential.user?.email;

      if (idToken == null || email == null) {
        throw Exception("Failed to get ID token or email");
      }

      setState(() {
        _idToken = idToken;
        _email = email;
        debugPrint('handleGoogleSignIn: Successfully signed in, email: $email');
      });

      bool adminExists = await checkIfAdminExists(email);
      if (adminExists) {
        debugPrint(
          'handleGoogleSignIn: Admin exists, navigating to AdminMainScreen',
        );
        Navigator.of(context).pushReplacement(
          MaterialPageRoute(builder: (_) => const AdminMainScreen()),
        );
      } else {
        setState(() {
          isRegistering = true;
          _isLoading = false;
          debugPrint(
            'handleGoogleSignIn: Admin does not exist, showing registration form, _isLoading = false',
          );
        });
      }
    } catch (e) {
      setState(() {
        _errorMessage = e.toString();
        _isLoading = false;
        debugPrint(
          'handleGoogleSignIn: Error occurred: $e, _isLoading = false',
        );
      });
    }
  }

  Future<bool> checkIfAdminExists(String email) async {
    try {
      final response = await http.get(
        Uri.parse('$apiBaseUrl/Admin/get-all-admins'),
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer $_idToken',
        },
      );

      if (response.statusCode == 200) {
        final List admins = jsonDecode(response.body);
        final exists = admins.any((admin) => admin['email'] == email);
        debugPrint('checkIfAdminExists: Admin exists for $email: $exists');
        return exists;
      }
      debugPrint(
        'checkIfAdminExists: Failed to fetch admins, status: ${response.statusCode}',
      );
      return false;
    } catch (e) {
      debugPrint('checkIfAdminExists: Error: $e');
      return false;
    }
  }

  Future<void> registerAdmin() async {
    setState(() {
      _isLoading = true;
      _errorMessage = null;
      debugPrint('registerAdmin: Starting registration, _isLoading = true');
    });

    if (_secretCodeController.text != _adminSecretCode) {
      setState(() {
        _errorMessage = 'Invalid secret code. Please try again.';
        _isLoading = false;
        debugPrint('registerAdmin: Invalid secret code, _isLoading = false');
      });
      return;
    }

    try {
      final response = await http.post(
        Uri.parse('$apiBaseUrl/Auth/register/admin'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({
          'idToken': _idToken,
          'name': _nameController.text,
          'email': _email,
          'phone_number': _phoneController.text,
        }),
      );

      if (response.statusCode == 200) {
        debugPrint('registerAdmin: Registration successful');
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Registration successful!'),
            backgroundColor: accentColor,
          ),
        );
        Navigator.of(context).pushReplacement(
          MaterialPageRoute(builder: (_) => const AdminMainScreen()),
        );
      } else {
        setState(() {
          _errorMessage = 'Error: ${response.body}';
          _isLoading = false;
          debugPrint(
            'registerAdmin: Failed with status ${response.statusCode}, _isLoading = false',
          );
        });
      }
    } catch (e) {
      setState(() {
        _errorMessage = 'Error: $e';
        _isLoading = false;
        debugPrint('registerAdmin: Error: $e, _isLoading = false');
      });
    }
  }

  @override
  void dispose() {
    _nameController.dispose();
    _phoneController.dispose();
    _secretCodeController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final size = MediaQuery.of(context).size;
    debugPrint(
      'build: isRegistering = $isRegistering, _isLoading = $_isLoading',
    );
    return Scaffold(
      backgroundColor: secondaryColor.withOpacity(0.3),
      body: Center(
        child: Container(
          width: size.width > 600 ? 400 : size.width * 0.9,
          padding: const EdgeInsets.all(24),
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(12),
            boxShadow: const [
              BoxShadow(
                color: Colors.black12,
                blurRadius: 8,
                offset: Offset(0, 2),
              ),
            ],
          ),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              const SizedBox(height: 16),
              Icon(Icons.admin_panel_settings, size: 64, color: primaryColor),
              const SizedBox(height: 16),
              Text(
                isRegistering ? 'Complete Registration' : 'Admin Login',
                style: TextStyle(
                  fontSize: 22,
                  fontWeight: FontWeight.w600,
                  color: primaryColor,
                ),
              ),
              const SizedBox(height: 24),
              if (isRegistering) ...[
                TextField(
                  controller: _nameController,
                  decoration: InputDecoration(
                    labelText: 'Name',
                    labelStyle: TextStyle(color: primaryColor),
                    prefixIcon: Icon(Icons.person_outline, color: primaryColor),
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
                  style: const TextStyle(color: textColorDark),
                ),
                const SizedBox(height: 12),
                TextField(
                  controller: _phoneController,
                  decoration: InputDecoration(
                    labelText: 'Phone Number',
                    labelStyle: TextStyle(color: primaryColor),
                    prefixIcon: Icon(Icons.phone, color: primaryColor),
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
                  style: const TextStyle(color: textColorDark),
                  keyboardType: TextInputType.phone,
                ),
                const SizedBox(height: 12),
                TextField(
                  controller: _secretCodeController,
                  decoration: InputDecoration(
                    labelText: 'Secret Code',
                    labelStyle: TextStyle(color: primaryColor),
                    prefixIcon: Icon(Icons.lock_outline, color: primaryColor),
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
                  obscureText: true,
                  style: const TextStyle(color: textColorDark),
                ),
                const SizedBox(height: 16),
                if (_errorMessage != null)
                  Padding(
                    padding: const EdgeInsets.only(bottom: 12),
                    child: Text(
                      _errorMessage!,
                      style: const TextStyle(color: Colors.red),
                    ),
                  ),
                _isLoading
                    ? CircularProgressIndicator(color: primaryColor)
                    : ElevatedButton(
                      onPressed: registerAdmin,
                      style: ElevatedButton.styleFrom(
                        backgroundColor: primaryColor,
                        foregroundColor: textColorLight,
                        padding: const EdgeInsets.symmetric(
                          horizontal: 32,
                          vertical: 12,
                        ),
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(8),
                        ),
                        elevation: 2,
                      ),
                      child: const Text(
                        'Register',
                        style: TextStyle(fontSize: 16),
                      ),
                    ),
              ] else ...[
                ElevatedButton.icon(
                  onPressed: _isLoading ? null : handleGoogleSignIn,
                  icon: const Icon(Icons.login, color: textColorLight),
                  label: const Text(
                    'Sign in with Google',
                    style: TextStyle(color: textColorLight),
                  ),
                  style: ElevatedButton.styleFrom(
                    backgroundColor: accentColor,
                    foregroundColor: textColorLight,
                    padding: const EdgeInsets.symmetric(
                      horizontal: 24,
                      vertical: 12,
                    ),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(8),
                    ),
                    elevation: 2,
                  ),
                ),
                const SizedBox(height: 12),
                if (_errorMessage != null)
                  Padding(
                    padding: const EdgeInsets.only(bottom: 12),
                    child: Text(
                      _errorMessage!,
                      style: const TextStyle(color: Colors.red),
                    ),
                  ),
                Text(
                  'If you don\'t have an account — it will be created after Google sign-in.',
                  style: TextStyle(
                    fontSize: 12,
                    color: primaryColor.withOpacity(0.7),
                  ),
                  textAlign: TextAlign.center,
                ),
              ],
            ],
          ),
        ),
      ),
    );
  }
}
