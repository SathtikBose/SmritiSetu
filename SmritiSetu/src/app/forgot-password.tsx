import { View, Text, TouchableOpacity, TextInput, SafeAreaView, KeyboardAvoidingView, Platform } from 'react-native';
import { useRouter } from 'expo-router';
import { useState } from 'react';
import { ArrowLeft } from 'lucide-react-native';

export default function ForgotPasswordScreen() {
  const router = useRouter();
  const [email, setEmail] = useState('');

  return (
    <SafeAreaView className="flex-1 bg-slate-950">
      <KeyboardAvoidingView 
        behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
        className="flex-1 justify-center px-6"
      >
        <View className="absolute top-1/4 left-10 w-64 h-64 bg-indigo-500/20 rounded-full blur-3xl opacity-40" />
        
        <View className="bg-white/10 border border-white/20 p-8 rounded-3xl z-10 shadow-xl">
          <TouchableOpacity 
            className="w-10 h-10 bg-white/10 rounded-full items-center justify-center mb-6 border border-white/10"
            onPress={() => router.back()}
          >
            <ArrowLeft color="#fff" size={20} />
          </TouchableOpacity>
          
          <Text className="text-3xl font-bold text-white mb-2">Reset Password</Text>
          <Text className="text-slate-400 mb-8 font-medium">Enter your email and we'll send you a verification code.</Text>
          
          <View className="space-y-4 mb-8">
            <View>
              <Text className="text-slate-300 mb-2 font-medium ml-1">Email</Text>
              <TextInput 
                className="bg-slate-900/50 border border-slate-700 text-white px-4 py-4 rounded-xl text-base"
                placeholder="Enter your email"
                placeholderTextColor="#64748b"
                keyboardType="email-address"
                autoCapitalize="none"
                value={email}
                onChangeText={setEmail}
              />
            </View>
          </View>
          
          <TouchableOpacity 
            className="bg-indigo-500 w-full py-4 rounded-xl items-center shadow-lg shadow-indigo-500/30"
            onPress={() => router.push('/otp')}
          >
            <Text className="text-white font-bold text-lg">Send Code</Text>
          </TouchableOpacity>
        </View>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}
