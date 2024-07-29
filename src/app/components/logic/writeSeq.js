import { useReducer } from 'react';

const MAX_IMAGES = 4;
const MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB in bytes
const MAX_TITLE_LENGTH = 100;
const MAX_CONTENT_LENGTH = 1000;
const MAX_SHORT_VIDEO_DURATION = 60; // 1 minute in seconds
const MAX_SHORT_VIDEO_SIZE = 100 * 1024 * 1024; // 100MB in bytes

const initialState = {
  step: 1,
  title: '',
  type: '광고',
  content: '',
  discountType: '',
  discountValue: '',
  bundleDiscountType: '',
  bundleDiscountValue: '',
  imageFiles: [],
  shortVideo: null,
  platforms: [],
  profilePublic: false,
  platformLinks: {},
  showPlatformSelection: false,
  tags: [],
  monetize: null,
  errors: {
    title: '',
    content: '',
    images: [],
    shortVideo: '',
  },
};

function postReducer(state, action) {
  switch (action.type) {
    case 'SET_STEP':
      return { ...state, step: action.payload };
    case 'SET_TITLE':
      return { 
        ...state, 
        title: action.payload.slice(0, MAX_TITLE_LENGTH),
        errors: {
          ...state.errors,
          title: action.payload.length > MAX_TITLE_LENGTH ? '제목은 최대 100자까지 입력 가능합니다.' : ''
        }
      };
    case 'SET_TYPE':
      return { ...state, type: action.payload };
    case 'SET_CONTENT':
      return { 
        ...state, 
        content: action.payload.slice(0, MAX_CONTENT_LENGTH),
        errors: {
          ...state.errors,
          content: action.payload.length > MAX_CONTENT_LENGTH ? '내용은 최대 1000자까지 입력 가능합니다.' : ''
        }
      };
    case 'ADD_IMAGE':
      if (state.imageFiles.length >= MAX_IMAGES) {
        return {
          ...state,
          errors: {
            ...state.errors,
            images: [...state.errors.images, "최대 4개의 이미지만 업로드할 수 있습니다."]
          }
        };
      }
      if (action.payload.size > MAX_IMAGE_SIZE) {
        return {
          ...state,
          errors: {
            ...state.errors,
            images: [...state.errors.images, `${action.payload.name}의 크기가 5MB를 초과합니다.`]
          }
        };
      }
      return {
        ...state,
        imageFiles: [...state.imageFiles, action.payload],
        errors: {
          ...state.errors,
          images: []
        }
      };
    case 'SET_SHORT_VIDEO':
      if (action.payload.duration > MAX_SHORT_VIDEO_DURATION) {
        return {
          ...state,
          errors: {
            ...state.errors,
            shortVideo: '숏폼 영상은 1분을 초과할 수 없습니다.'
          }
        };
      }
      if (action.payload.size > MAX_SHORT_VIDEO_SIZE) {
        return {
          ...state,
          errors: {
            ...state.errors,
            shortVideo: '숏폼 영상의 크기가 100MB를 초과합니다.'
          }
        };
      }
      return {
        ...state,
        shortVideo: action.payload,
        errors: {
          ...state.errors,
          shortVideo: ''
        }
      };
    case 'SET_PLATFORMS':
      return { ...state, platforms: action.payload };
    case 'SET_DISCOUNTTYPE':
      return { ...state, discountType: action.payload };
    case 'SET_DISCOUNTVALUE':
      return { ...state, discountValue: action.payload };
    case 'SET_BUNDLE_DISCOUNT_TYPE':
      return { ...state, bundleDiscountType: action.payload };
    case 'SET_BUNDLE_DISCOUNT_VALUE':
      return { ...state, bundleDiscountValue: action.payload };
    case 'SET_PROFILE_PUBLIC':
      return { ...state, profilePublic: action.payload };
    case 'SET_PLATFORM_LINKS':
      return { ...state, platformLinks: { ...state.platformLinks, ...action.payload } };
    case 'TOGGLE_PLATFORM_SELECTION':
      return { ...state, showPlatformSelection: !state.showPlatformSelection };
    case 'SET_MONETIZE':
      return { ...state, monetize: action.payload };
    case 'CLEAR_ERRORS':
      return { ...state, errors: initialState.errors };
    default:
      return state;
  }
}

function usePostCreation() {
  const [state, dispatch] = useReducer(postReducer, initialState);

  const setStep = (step) => dispatch({ type: 'SET_STEP', payload: step });
  const setTitle = (title) => dispatch({ type: 'SET_TITLE', payload: title });
  const setContent = (content) => dispatch({ type: 'SET_CONTENT', payload: content });
  const addImage = (image) => dispatch({ type: 'ADD_IMAGE', payload: image });
  const setShortVideo = (video) => dispatch({ type: 'SET_SHORT_VIDEO', payload: video });
  const clearErrors = () => dispatch({ type: 'CLEAR_ERRORS' });
  const setType = (type) => dispatch({ type: 'SET_TYPE', payload: type });
  const setPlatforms = (platforms) => dispatch({ type: 'SET_PLATFORMS', payload: platforms });
  const setProfilePublic = (isPublic) => dispatch({ type: 'SET_PROFILE_PUBLIC', payload: isPublic });
  const setPlatformLink = (platform, link) => dispatch({ type: 'SET_PLATFORM_LINKS', payload: { [platform]: link } });
  const togglePlatformSelection = () => dispatch({ type: 'TOGGLE_PLATFORM_SELECTION' });
  const setMonetize = (monetize) => dispatch({ type: 'SET_MONETIZE', payload: monetize });
  const setDiscountType = (discountType) => dispatch({ type: 'SET_DISCOUNTTYPE', payload: discountType });
  const setDiscountValue = (discountValue) => dispatch({ type: 'SET_DISCOUNTVALUE', payload: discountValue });
  const setBundleDiscountType = (bundleDiscountType) => dispatch({ type: 'SET_BUNDLE_DISCOUNT_TYPE', payload: bundleDiscountType });
  const setBundleDiscountValue = (bundleDiscountValue) => dispatch({ type: 'SET_BUNDLE_DISCOUNT_VALUE', payload: bundleDiscountValue });

  return {
    state,
    setStep,
    clearErrors,
    setTitle,
    setType,
    setDiscountType,
    setDiscountValue,
    setBundleDiscountType,
    setBundleDiscountValue,
    setContent,
    addImage,
    setShortVideo,
    setPlatforms,
    setProfilePublic,
    setPlatformLink,
    togglePlatformSelection,
    setMonetize,
  };
}

export default usePostCreation;
