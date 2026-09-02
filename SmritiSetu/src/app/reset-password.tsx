import { View, Text, TouchableOpacity, TextInput, SafeAreaView, KeyboardAvoidingView, Platform } from 'react-native';
import { useRouter } from 'expo-router';
import { useState } from 'react';
import { ArrowLeft } from 'lucide-react-native';

export default function ResetPasswordScreen() {
  const router = useRouter();
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  return (
    <SafeAreaView className="flex-1 bg-slate-950">
      <KeyboardAvoidingView 
        behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
        className="flex-1 justify-center px-6"
      >
        <View className="absolute bottom-1/4 left-10 w-64 h-64 bg-purple-500/20 rounded-full blur-3xl opacity-40" />
        
        <View className="bg-white/10 border border-white/20 p-8 rounded-3xl z-10 shadow-xl">
          <TouchableOpacity 
            className="w-10 h-10 bg-white/10 rounded-full items-center justify-center mb-6 border border-white/10"
            onPress={() => router.back()}
          >
            <ArrowLeft color="#fff" size={20} />
          </TouchableOpacity>
          
          <Text className="text-3xl font-bold text-white mb-2">New Password</Text>
          <Text className="text-slate-400 mb-8 font-medium">Create a new secure password for your account.</Text>
          
          <View className="space-y-4 mb-8">
            <View>
              <Text className="text-slate-300 mb-2 font-medium ml-1">New Password</Text>
              <TextInput 
                className="bg-slate-900/50 border border-slate-700 text-white px-4 py-4 rounded-xl text-base"
                placeholder="Enter new password"
                placeholderTextColor="#64748b"
                secureTextEntry
                value={password}
                onChangeText={setPassword}
              />
            </View>
            
            <View className="mt-4">
              <Text className="text-slate-300 mb-2 font-medium ml-1">Confirm Password</Text>
              <TextInput 
                className="bg-slate-900/50 border border-slate-700 text-white px-4 py-4 rounded-xl text-base"
                placeholder="Confirm new password"
                placeholderTextColor="#64748b"
                secureTextEntry
                value={confirmPassword}
                onChangeText={setConfirmPassword}
              />
            </View>
          </View>
          
          <TouchableOpacity 
            className="bg-indigo-500 w-full py-4 rounded-xl items-center shadow-lg shadow-indigo-500/30"
            onPress={() => router.replace('/login')}
          >
            <Text className="text-white font-bold text-lg">Reset Password</Text>
          </TouchableOpacity>
        </View>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}
