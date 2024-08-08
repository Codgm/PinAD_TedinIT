"use client";

import React, { useState } from 'react';
import Styles from '@/app/styles/AttractionComponent.module.css';

const Attraction = () => {
    const [attractionData, setAttractionData] = useState({
        title: '',
        location: '',
        details: {
            intro: '',
            best_time: '',
            special_reasons: [''],  
            must_do: [''],          
            photo_spot: '',
            tips: [''],            
            recomment_actions: [''],
            summary: ''
        }
    });

    const handleChange = (e) => {
        const { name, value } = e.target;
        setAttractionData(prevState => ({
            ...prevState,
            [name]: value
        }));
    };

    const handleListChange = (e, index, field) => {
        const newArray = [...attractionData[field]];
        newArray[index] = e.target.value;
        setAttractionData(prevState => ({
            ...prevState,
            [field]: newArray
        }));
    };

    const addField = (field) => {
        setAttractionData(prevState => {
            const updatedArray = [...prevState[field]];
            if (field === 'special_reasons' && updatedArray.length < 3) {
                updatedArray.push('');
            } else if (field !== 'special_reasons' && updatedArray.length < 3) {
                updatedArray.push('');
            }
            return {
                ...prevState,
                [field]: updatedArray
            };
        });
    };

    const removeField = (field, index) => {
        setAttractionData(prevState => {
            const updatedArray = [...prevState[field]];
            if (updatedArray.length > 1) {
                updatedArray.splice(index, 1);
            }
            return {
                ...prevState,
                [field]: updatedArray
            };
        });
    };

    return (
        <div className={Styles.container}>
            <form className={Styles.form}>
                <div className='flex items-center w-full space-x-2'>
                    <div className={Styles.formGroup}>
                        <input
                            type="text"
                            id="title"
                            name="title"
                            placeholder='명소 이름'
                            value={attractionData.name}
                            onChange={handleChange}
                            required
                        />
                    </div>

                    <div className={Styles.formGroup}>
                        <input
                            type="text"
                            id="location"
                            name="location"
                            placeholder='명소 위치'
                            value={attractionData.location}
                            onChange={handleChange}
                            required
                        />
                    </div>
                </div>
                <div className={Styles.formGroup}>
                    <input
                        type="text"
                        id="intro"
                        name="intro"
                        placeholder='한 줄 소개'
                        value={attractionData.intro}
                        onChange={handleChange}
                        required
                    />
                </div>
                <div className={Styles.formGroup}>
                <label htmlFor="best_time">최적의 방문 시기</label>
                    <input
                        type="text"
                        id="best_time"
                        name="best_time"
                        placeholder='계절, 월, 또는 시간대'
                        value={attractionData.best_time}
                        onChange={handleChange}
                        required
                    />
                </div>
                <div className={Styles.formGroup}>
                    <label>이곳이 특별한 이유</label>
                    {attractionData.details.special_reasons.map((item, index) => (
                        <div key={index} className={Styles.fieldContainer}>
                            <input
                                type="text"
                                placeholder={`주요 특징 ${index + 1}`}
                                value={item}
                                onChange={(e) => handleListChange(e, index, 'special_reasons')}
                                required
                            />
                        </div>
                    ))}
                    <button
                        type="button"
                        onClick={() => addField('special_reasons')}
                        className={Styles.addButton}
                    >
                        추가
                    </button>
                    {attractionData.details.special_reasons.length > 1 && (
                        <button
                            type="button"
                            onClick={() => removeField('special_reasons', attractionData.special_reasons.length - 1)}
                            className={Styles.removeButton}
                        >
                            삭제
                        </button>
                    )}
                </div>

                <div className={Styles.formGroup}>
                    <label>꼭 해봐야 할 것들</label>
                    {attractionData.details.must_do.map((item, index) => (
                        <div key={index} className={Styles.fieldContainer}>
                            <input
                                type="text"
                                placeholder={`활동 또는 경험 ${index + 1}`}
                                value={item}
                                onChange={(e) => handleListChange(e, index, 'must_do')}
                                required
                            />
                        </div>
                    ))}
                    <button
                        type="button"
                        onClick={() => addField('must_do')}
                        className={Styles.addButton}
                    >
                        추가
                    </button>
                    {attractionData.details.must_do.length > 1 && (
                        <button
                            type="button"
                            onClick={() => removeField('must_do', attractionData.must_do.length - 1)}
                            className={Styles.removeButton}
                        >
                            삭제
                        </button>
                    )}
                </div>

                <div className={Styles.formGroup}>
                    <label>알아두면 좋은 팁</label>
                    {attractionData.details.tips.map((item, index) => (
                        <div key={index} className={Styles.fieldContainer}>
                            <input
                                type="text"
                                placeholder={`유용한 정보 ${index + 1}`}
                                value={item}
                                onChange={(e) => handleListChange(e, index, 'tips')}
                            />
                        </div>
                    ))}
                    <button
                        type="button"
                        onClick={() => addField('tips')}
                        className={Styles.addButton}
                    >
                        추가
                    </button>
                    {attractionData.details.tips.length > 1 && (
                        <button
                            type="button"
                            onClick={() => removeField('tips', attractionData.tips.length - 1)}
                            className={Styles.removeButton}
                        >
                            삭제
                        </button>
                    )}
                </div>

                <div className={Styles.formGroup}>
                    <label>이런 분들에게 추천해요</label>
                    {attractionData.details.recomment_actions.map((item, index) => (
                        <div key={index} className={Styles.fieldContainer}>
                            <input
                                type="text"
                                placeholder={`추천 대상 ${index + 1}`}
                                value={item}
                                onChange={(e) => handleListChange(e, index, 'recomment_actions')}
                            />
                        </div>
                    ))}
                    <button
                        type="button"
                        onClick={() => addField('recomment_actions')}
                        className={Styles.addButton}
                    >
                        추가
                    </button>
                    {attractionData.details.recomment_actions.length > 1 && (
                        <button
                            type="button"
                            onClick={() => removeField('recomment_actions', attractionData.recomment_actions.length - 1)}
                            className={Styles.removeButton}
                        >
                            삭제
                        </button>
                    )}
                </div>

                <div className={Styles.formGroup}>
                    <label htmlFor="photo_spot">인생샷 스팟</label>
                    <input
                        type="text"
                        id="photo_spot"
                        name="photo_spot"
                        placeholder='간단한 위치 설명'
                        value={attractionData.photo_spot}
                        onChange={handleChange}
                        required
                    />
                </div>

                <div className={Styles.formGroup}>
                    <label htmlFor="summary">🗣 한마디로 표현하자면</label>
                    <input
                        type="text"
                        id="summary"
                        name="summary"
                        placeholder='이 장소를 함축적으로 표현하는 짧은 문구'
                        value={attractionData.summary}
                        onChange={handleChange}
                        required
                    />
                </div>
            </form>
        </div>
    );
};

export default Attraction;
