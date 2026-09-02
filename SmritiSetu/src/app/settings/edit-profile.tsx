import { View, Text, TouchableOpacity, TextInput, SafeAreaView, KeyboardAvoidingView, Platform, ScrollView } from 'react-native';
import { useRouter } from 'expo-router';
import { useState } from 'react';
import { ArrowLeft, User } from 'lucide-react-native';

export default function EditProfileScreen() {
  const router = useRouter();
  const [name, setName] = useState('Jane Doe');
  const [bio, setBio] = useState('Loving memory games and puzzles!');
  const [email, setEmail] = useState('jane.doe@example.com');

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
          <Text className="text-3xl font-bold text-white">Edit Profile</Text>
        </View>

        <ScrollView showsVerticalScrollIndicator={false} contentContainerStyle={{ paddingBottom: 40 }}>
          <View className="bg-white/10 border border-white/20 p-8 rounded-3xl items-center shadow-xl mb-6">
            <View className="relative mb-6">
              <View className="w-24 h-24 bg-indigo-500/30 rounded-full items-center justify-center border-2 border-indigo-400">
                <User color="#818cf8" size={40} />
              </View>
              <TouchableOpacity className="absolute bottom-0 right-0 bg-indigo-500 w-8 h-8 rounded-full items-center justify-center border-2 border-slate-900">
                <Text className="text-white font-bold">+</Text>
              </TouchableOpacity>
            </View>

            <View className="w-full space-y-4">
              <View>
                <Text className="text-slate-300 mb-2 font-medium ml-1">Full Name</Text>
                <TextInput 
                  className="bg-slate-900/50 border border-slate-700 text-white px-4 py-4 rounded-xl text-base"
                  value={name}
                  onChangeText={setName}
                />
              </View>
              
              <View className="mt-4">
                <Text className="text-slate-300 mb-2 font-medium ml-1">Email</Text>
                <TextInput 
                  className="bg-slate-900/50 border border-slate-700 text-white px-4 py-4 rounded-xl text-base"
                  keyboardType="email-address"
                  autoCapitalize="none"
                  value={email}
                  onChangeText={setEmail}
                />
              </View>
              
              <View className="mt-4">
                <Text className="text-slate-300 mb-2 font-medium ml-1">Bio</Text>
                <TextInput 
                  className="bg-slate-900/50 border border-slate-700 text-white px-4 py-4 rounded-xl text-base"
                  multiline
                  numberOfLines={3}
                  value={bio}
                  onChangeText={setBio}
                  textAlignVertical="top"
                />
              </View>
            </View>
          </View>
          
          <TouchableOpacity 
            className="bg-indigo-500 w-full py-4 rounded-xl items-center shadow-lg shadow-indigo-500/30"
            onPress={() => router.back()}
          >
            <Text className="text-white font-bold text-lg">Save Changes</Text>
          </TouchableOpacity>
        </ScrollView>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}
