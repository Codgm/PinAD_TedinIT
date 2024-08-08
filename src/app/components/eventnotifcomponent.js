import styles from '@/app/styles/EventTemplete.module.css';
import { useState } from 'react';
import usePostCreation from './logic/writeSeq';

const EventNotification = () => {

    const {
      state,
      updateDetailsField} =usePostCreation()

    const [eventData, setEventData] = useState({
        title: '',
        location: '',
        details: {
            date: '',
            time: '',
            organizer: '',
            organizerContact: '',
            registrationMethod: '',
            registrationDeadline: '',
            eventDescription: '',
            fee: 'free',
            inquiryContact: '',
            additionalInfo: '',
            registrationLink: ''
        }
    })

    const handleChange = (e) => {
        const { name, value } = e.target
        if (name === 'title' || name === 'location') {
            setEventData(prevState => ({
                ...prevState,
                [name]: value
            }))
        } else {
            setEventData(prevState => ({
                ...prevState,
                details: {
                    ...prevState.details,
                    [name]: value
                }
            }))
            updateDetailsField(name, value)
        }
        console.log(state.details)
    }
    const handleSubmit = (e) => {
        e.preventDefault()
        console.log(state.details)
        console.log('제출된 행사 정보:', eventData)
        // 여기에 데이터 처리 로직 추가 (예: API로 전송)
    }
    
    return (
        <div className={styles.container}>
          <form onSubmit={handleSubmit} className={styles.form}>
            <div className={styles.formGroup}>
              <label htmlFor="title">행사 일정</label>
              <input
                type="text"
                id="title"
                name="title"
                placeholder='행사 제목'
                value={eventData.title}
                onChange={handleChange}
                required
              />
            </div>
    
            <div className="flex items-center w-full space-x-2">
                <div className={styles.formGroup}>
                <input
                    type="date"
                    id="date"
                    name="date"
                    value={eventData.date}
                    onChange={handleChange}
                    required
                />
                </div>

                <div className={styles.formGroup}>
                <input
                    type="time"
                    id="time"
                    name="time"
                    value={eventData.time}
                    onChange={handleChange}
                    required
                />
                </div>
            </div>
    
            <div className={styles.formGroup}>
              <input
                placeholder='행사 장소'
                type="text"
                id="location"
                name="location"
                value={eventData.location}
                onChange={handleChange}
                required
              />
            </div>

            <div className={styles.formGroup}>
              <label htmlFor="eventDescription">행사 내용</label>
              <textarea
                id="eventDescription"
                name="eventDescription"
                value={eventData.eventDescription}
                onChange={handleChange}
                required
              />
            </div>

            <div className={styles.formGroup}>
              <label htmlFor="registrationMethod">참가 방법</label>
              <textarea
                id="registrationMethod"
                name="registrationMethod"
                value={eventData.registrationMethod}
                onChange={handleChange}
                required
              />
            </div>
            <label 
                  htmlFor="paymentType"
                  className="block text-ml font-bold text-black-600"  
                >참가 비용</label>

            <div className="flex items-center w-full space-x-2">
                <div className={styles.formGroup}>
                  <select
                    id="fee"
                    name="fee"
                    className="w-full border rounded-lg p-2"
                    value={eventData.details.fee}
                    onChange={handleChange}
                    >
                        <option value="free">무료</option>
                        <option value="paid">유료</option>
                  </select>
                </div>
                {eventData.details.fee !='free' && (
                    <div className={styles.formGroup}>
                        <input
                            placeholder='참가비'
                            type="text"
                            id="fee"
                            name="fee"
                            className="w-full border rounded-lg p-2"
                            onChange={handleChange}
                        />  
                    </div>
                )}
            </div>
            <div className={styles.formGroup}>
              <label htmlFor="registrationDeadline">등록 마감일</label>
              <input
                type="date"
                id="registrationDeadline"
                name="registrationDeadline"
                value={eventData.registrationDeadline}
                onChange={handleChange}
              />
            </div>

            <label 
              htmlFor="organizer"
              className="block text-ml font-bold text-black-600"
            >주최 정보</label>
            <div className="flex items-center w-full space-x-2">
                <div className={styles.formGroup}>
                <input
                    placeholder='주최자'
                    type="text"
                    id="organizer"
                    name="organizer"
                    value={eventData.organizer}
                    onChange={handleChange}
                    required
                />
                </div>
                <div className={styles.formGroup}>
                <input
                    placeholder='주최 연락처'
                    type="text"
                    id="organizerContact"
                    name="organizerContact"
                    value={eventData.organizerContact}
                    onChange={handleChange}
                    required
                />
                </div>
            </div>
            <div className={styles.formGroup}>
              <input
                placeholder='문의 연락처'
                type="text"
                id="inquiryContact"
                name="inquiryContact"
                value={eventData.inquiryContact}
                onChange={handleChange}
                required
              />
            </div>
    
            <div className={styles.formGroup}>
              <textarea
                placeholder='추가 정보'
                id="additionalInfo"
                name="additionalInfo"
                value={eventData.additionalInfo}
                onChange={handleChange}
              />
            </div>
    
            <div className={styles.formGroup}>
              <input
                placeholder='등록 링크'
                type="url"
                id="registrationLink"
                name="registrationLink"
                value={eventData.registrationLink}
                onChange={handleChange}
              />
            </div>    
            <button type="submit" className={styles.submitButton} onClick={()=>handleSubmit()}>
              행사 등록
            </button>
          </form>
    </div>
)}

export default EventNotification