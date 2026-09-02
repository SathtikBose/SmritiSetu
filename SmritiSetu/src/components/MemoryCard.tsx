import React, { useEffect } from 'react';
import { TouchableOpacity, StyleSheet, View } from 'react-native';
import Animated, { 
  useAnimatedStyle, 
  withTiming, 
  withSequence, 
  useSharedValue, 
  withSpring,
  interpolate
} from 'react-native-reanimated';

export type CardState = 'down' | 'up' | 'matched';

export interface CardData {
  id: number;
  iconId: number;
  state: CardState;
}

interface MemoryCardProps {
  card: CardData;
  onPress: (id: number) => void;
  size: number;
  mismatchShake?: boolean;
}

export const MemoryCard = React.memo(function MemoryCard({ card, onPress, size, mismatchShake }: MemoryCardProps) {
  const rotation = useSharedValue(0);
  const scale = useSharedValue(1);
  const translateX = useSharedValue(0);

  useEffect(() => {
    if (card.state === 'up' || card.state === 'matched') {
      rotation.value = withSpring(180, { damping: 12, stiffness: 100 });
    } else {
      rotation.value = withSpring(0, { damping: 12, stiffness: 100 });
    }
  }, [card.state]);

  useEffect(() => {
    if (mismatchShake) {
      translateX.value = withSequence(
        withTiming(-10, { duration: 50 }),
        withTiming(10, { duration: 50 }),
        withTiming(-10, { duration: 50 }),
        withTiming(0, { duration: 50 })
      );
    }
  }, [mismatchShake]);

  const frontAnimatedStyle = useAnimatedStyle(() => {
    const rotateY = interpolate(rotation.value, [0, 180], [0, 180]);
    return {
      transform: [
        { perspective: 1000 },
        { rotateY: `${rotateY}deg` },
        { scale: scale.value },
        { translateX: translateX.value }
      ],
      opacity: card.state === 'matched' ? 0.6 : 1,
      zIndex: card.state === 'up' ? 2 : 1,
    };
  });

  const backAnimatedStyle = useAnimatedStyle(() => {
    const rotateY = interpolate(rotation.value, [0, 180], [180, 360]);
    return {
      transform: [
        { perspective: 1000 },
        { rotateY: `${rotateY}deg` },
        { scale: scale.value },
        { translateX: translateX.value }
      ],
      opacity: card.state === 'matched' ? 0.6 : 1,
      zIndex: card.state === 'up' ? 1 : 2,
    };
  });

  const handlePress = () => {
    if (card.state !== 'down') return;
    scale.value = withSequence(
      withTiming(0.95, { duration: 100 }),
      withTiming(1, { duration: 100 })
    );
    onPress(card.id);
  };

  const getCardColor = (iconId: number) => {
    const colors = ['#ef4444', '#f97316', '#f59e0b', '#10b981', '#3b82f6', '#6366f1', '#8b5cf6', '#ec4899', '#14b8a6', '#06b6d4'];
    return colors[iconId % colors.length];
  };

  return (
    <TouchableOpacity 
      activeOpacity={1} 
      onPress={handlePress}
      style={{ width: size, height: size, margin: 4 }}
    >
      <View style={styles.cardContainer}>
        {/* Front of card (Face Down) */}
        <Animated.View 
          style={[
            styles.card, 
            styles.front, 
            { width: size, height: size },
            frontAnimatedStyle
          ]}
        >
          <View className="w-full h-full bg-indigo-500/20 border-2 border-indigo-500/50 rounded-2xl items-center justify-center">
            <View className="w-1/2 h-1/2 bg-indigo-500/30 rounded-full" />
          </View>
        </Animated.View>

        {/* Back of card (Face Up / Revealed) */}
        <Animated.View 
          style={[
            styles.card, 
            styles.back, 
            { width: size, height: size },
            backAnimatedStyle
          ]}
        >
          <View 
            className="w-full h-full bg-white border-2 border-white/20 rounded-2xl items-center justify-center shadow-lg"
          >
            <View 
              style={{ backgroundColor: getCardColor(card.iconId) }}
              className="w-1/2 h-1/2 rounded-full" 
            />
          </View>
        </Animated.View>
      </View>
    </TouchableOpacity>
  );
});

const styles = StyleSheet.create({
  cardContainer: {
    flex: 1,
  },
  card: {
    position: 'absolute',
    backfaceVisibility: 'hidden',
    justifyContent: 'center',
    alignItems: 'center',
  },
  front: {
    backgroundColor: 'transparent',
  },
  back: {
    backgroundColor: 'transparent',
  },
});
