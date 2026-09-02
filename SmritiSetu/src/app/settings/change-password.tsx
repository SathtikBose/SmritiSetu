import { View, Text, TouchableOpacity, TextInput, SafeAreaView, KeyboardAvoidingView, Platform, ScrollView } from 'react-native';
import { useRouter } from 'expo-router';
import { useState } from 'react';
import { ArrowLeft } from 'lucide-react-native';

export default function ChangePasswordScreen() {
  const router = useRouter();
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  return (
    <SafeAreaView className="flex-1 bg-slate-950">
      <KeyboardAvoidingView 
        behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
        className="flex-1 px-6 pt-8"
      >
        <View className="flex-row items-center mb-8">
          <TouchableOpacity 
            className="w-10 h-10 bg-white/10 rounded-full items-center justify-center border border-white/10 mr-4"
            onPress={() => router.back()}
          >
            <ArrowLeft color="#fff" size={20} />
          </TouchableOpacity>
          <Text className="text-3xl font-bold text-white">Change Password</Text>
        </View>

        <ScrollView showsVerticalScrollIndicator={false}>
          <View className="bg-white/10 border border-white/20 p-8 rounded-3xl shadow-xl mb-6">
            <View className="space-y-4">
              <View>
                <Text className="text-slate-300 mb-2 font-medium ml-1">Current Password</Text>
                <TextInput 
                  className="bg-slate-900/50 border border-slate-700 text-white px-4 py-4 rounded-xl text-base"
                  secureTextEntry
                  placeholder="Enter current password"
                  placeholderTextColor="#64748b"
                  value={currentPassword}
                  onChangeText={setCurrentPassword}
                />
              </View>
              
              <View className="mt-4">
                <Text className="text-slate-300 mb-2 font-medium ml-1">New Password</Text>
                <TextInput 
                  className="bg-slate-900/50 border border-slate-700 text-white px-4 py-4 rounded-xl text-base"
                  secureTextEntry
                  placeholder="Enter new password"
                  placeholderTextColor="#64748b"
                  value={newPassword}
                  onChangeText={setNewPassword}
                />
              </View>
              
              <View className="mt-4">
                <Text className="text-slate-300 mb-2 font-medium ml-1">Confirm New Password</Text>
                <TextInput 
                  className="bg-slate-900/50 border border-slate-700 text-white px-4 py-4 rounded-xl text-base"
                  secureTextEntry
                  placeholder="Confirm new password"
                  placeholderTextColor="#64748b"
                  value={confirmPassword}
                  onChangeText={setConfirmPassword}
                />
              </View>
            </View>
          </View>
          
          <TouchableOpacity 
            className="bg-indigo-500 w-full py-4 rounded-xl items-center shadow-lg shadow-indigo-500/30"
            onPress={() => router.back()}
          >
            <Text className="text-white font-bold text-lg">Update Password</Text>
          </TouchableOpacity>
        </ScrollView>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}
