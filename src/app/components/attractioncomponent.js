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

    // 텍스트 필드의 값을 업데이트하는 함수
    const handleChange = (e) => {
        const { name, value } = e.target;
        if (name in attractionData) {
            setAttractionData(prevState => ({
                ...prevState,
                [name]: value
            }));
        } else {
            setAttractionData(prevState => ({
                ...prevState,
                details: {
                    ...prevState.details,
                    [name]: value
                }
            }));
        }
    };

    // 리스트 항목의 값을 업데이트하는 함수
    const handleListChange = (e, index, field) => {
        setAttractionData(prevState => {
            const updatedDetails = { ...prevState.details };
            if (Array.isArray(updatedDetails[field])) {
                updatedDetails[field][index] = e.target.value;
            }
            return {
                ...prevState,
                details: updatedDetails
            };
        });
    };

    // 리스트 항목을 추가하는 함수
    const addField = (field) => {
        setAttractionData(prevState => {
            const updatedDetails = { ...prevState.details };
            if (Array.isArray(updatedDetails[field])) {
                if (updatedDetails[field].length < 3) {
                    updatedDetails[field] = [...updatedDetails[field], ''];
                }
            }
            return {
                ...prevState,
                details: updatedDetails
            };
        });
    };

    // 리스트 항목을 삭제하는 함수
    const removeField = (field, index) => {
        setAttractionData(prevState => {
            const updatedDetails = { ...prevState.details };
            if (Array.isArray(updatedDetails[field]) && updatedDetails[field].length > 1) {
                updatedDetails[field].splice(index, 1);
            }
            return {
                ...prevState,
                details: updatedDetails
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
                            value={attractionData.title}
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
                        value={attractionData.details.intro}
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
                        value={attractionData.details.best_time}
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
                            onClick={() => removeField('special_reasons', attractionData.details.special_reasons.length - 1)}
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
                            onClick={() => removeField('must_do', attractionData.details.must_do.length - 1)}
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
                            onClick={() => removeField('tips', attractionData.details.tips.length - 1)}
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
                            onClick={() => removeField('recomment_actions', attractionData.details.recomment_actions.length - 1)}
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
                        value={attractionData.details.photo_spot}
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
                        value={attractionData.details.summary}
                        onChange={handleChange}
                        required
                    />
                </div>
            </form>
        </div>
    );
};

export default Attraction;
