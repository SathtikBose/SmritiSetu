import { View, Text, TextInput, TouchableOpacity } from 'react-native';
import { Link, useRouter } from 'expo-router';
import { ArrowLeft } from 'lucide-react-native';

export default function ForgotPassword() {
  const router = useRouter();

  return (
    <View className="flex-1 px-6 pt-16 bg-slate-900">
      <TouchableOpacity 
        className="mb-8 h-10 w-10 justify-center"
        onPress={() => router.back()}
      >
        <ArrowLeft color="#818cf8" size={24} />
      </TouchableOpacity>

      <Text className="text-3xl font-bold text-indigo-400 mb-2">Reset Password</Text>
      <Text className="text-slate-400 mb-8">
        Enter your email address to receive a one-time password (OTP).
      </Text>
      
      <View className="space-y-4">
        <View className="mb-6">
          <Text className="text-slate-300 mb-2">Email</Text>
          <TextInput 
            className="w-full bg-slate-800 text-slate-100 px-4 py-3 rounded-lg border border-slate-700"
            placeholder="Enter your email"
            placeholderTextColor="#64748b"
            keyboardType="email-address"
            autoCapitalize="none"
          />
        </View>

        {/* @ts-ignore */}
        <Link href="/otp" asChild>
          <TouchableOpacity className="w-full bg-indigo-500 py-3 rounded-lg items-center mt-4">
            <Text className="text-white font-semibold text-lg">Send OTP</Text>
          </TouchableOpacity>
        </Link>
      </View>
    </View>
  );
}
