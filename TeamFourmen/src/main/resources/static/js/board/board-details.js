/**
 * board-details.js
 */

document.addEventListener('DOMContentLoaded', function() {
	
	const btnPostLike = document.querySelector('.btn-like-post');
	const spanLikeBtn = btnPostLike.querySelector('.span-like-btn');
	const spanNumLikes = document.querySelector('.span-num-likes');
	let numLikes = parseInt(spanNumLikes.textContent);
	
	// 게시글 삭제, 수정 버튼
	const btnDeletePost = document.querySelector('.btn-delete-post');
	const btnEditPost = document.querySelector('.btn-edit-post');
	
	// 로그인한 유저
	const user = document.querySelector('.div-profile-image');
	
	const postId = document.querySelector('.post-title').getAttribute('postId');
	const authorNickname = document.querySelector('.div-post-author').textContent;	
	
		
	// 댓글관련 속성
	const textareaPostComment = document.querySelector('.textarea-post-comment');
	const btnAddPostComment = document.querySelector('.btn-add-post-comment');
	
	const spanNumCommentsList = document.querySelectorAll('.span-num-comments');
	
	const btnCommentRefresh = document.querySelector('.btn-post-comment-refresh');
	
	const commentContainer = document.querySelector('.post-comment-container');
	
	
	// 게시글 카테고리(영화, 티비, 인물)
	const category = location.pathname.split('/')[1];
	

	// 게시글 디테일 페이지 띄웠을 때 좋아요 눌렀으면 좋아요버튼 좋아요 취소로 보여주기
	if (btnPostLike.classList.contains('liked-already')) {
		spanLikeBtn.textContent = '좋아요 취소';
	}
	
	
		// 게시글 삭제 버튼
	if (btnDeletePost) {	
		btnDeletePost.addEventListener('click', function() {
			const postId = btnDeletePost.getAttribute('postId');
			
			axios.delete(`/board/delete/${postId}`)
				.then(() => {
					alert('게시글 삭제 성공');
					location.href=`/${category}/board`;
				})
				.catch((error) => {
					console.log(`에러 발생!!! ${error}`);
				});
			
		});
	}
	
	// 게시글 수정버튼
	if(btnEditPost) {
		btnEditPost.addEventListener('click', function() {
			const divPostContent = document.querySelector('.div-post-content');
			const divTitle = document.querySelector('.post-title');			
			
			const formToEdit = document.createElement('form');
			formToEdit.method = 'post';
			formToEdit.action = `/${category}/board/edit`;
			formToEdit.classList.add('d-none');
			
			const inputTitle = document.createElement('input');
			inputTitle.type="text";
			inputTitle.name = 'title';
			inputTitle.setAttribute('value', divTitle.textContent);
						
			const inputContent = document.createElement('input');			
			inputContent.type="text";
			inputContent.name = 'content';
			inputContent.setAttribute('value', divPostContent.innerHTML)
			
			const inputPostId = document.createElement('input');
			inputPostId.type="text";
			inputPostId.name="postId";
			inputPostId.setAttribute('value', postId);
			
			console.log(inputPostId);
			
			
			formToEdit.append(inputTitle, inputContent, inputPostId);
			
			
			document.body.appendChild(formToEdit);
			formToEdit.submit();
			
		});
	}
	
	
	
	// 댓글 좋아요 이벤트리스너 등록하는 함수
	const registerCommentLikeEventListener = function() {
		const commentLikeContainerList = document.querySelectorAll('.post-comment-like-container');
		
		
		commentLikeContainerList.forEach((btnLike) => {
			
			const commentIdForBtn = btnLike.getAttribute('commentId');
			const commentEachLikeContainer = btnLike.closest('.post-comment-each-like-container');
			const spanNumCommentLikes = commentEachLikeContainer.querySelector('.span-num-comment-likes');
			let commentNumLikes = parseInt(spanNumCommentLikes.textContent);
			
			btnLike.addEventListener('click', function() {
				if(!user) {
					alert('로그인한 유저만 댓글 좋아요를 누를 수 있습니다.');
					return;
				}
				
				
				if (user.getAttribute('nickname') === btnLike.getAttribute('author')) {
					alert('본인글에 좋아요를 누를 수 없습니다.');
					return;
				}
				
				const data = {
					member: {
						email: user.getAttribute('email') 
					},
					comment: {
						commentId: commentIdForBtn
					}				
				};
				
				if (btnLike.classList.contains('post-comment-already-liked')) {	// 이미 좋아요 누른 경우
					axios.post('/board/comment/like/delete', data)
						.then((response) => {										
							btnLike.classList.remove('post-comment-already-liked');
							commentNumLikes--;
							spanNumCommentLikes.textContent = commentNumLikes;
						})
						.catch((error) => {
							console.log(`에러 발생!!! ${error}`);
						})			
				} else {	// 좋아요 안누른 상태인 경우
					axios.post('/board/comment/like/add', data)
						.then((response) => {
							btnLike.classList.add('post-comment-already-liked');
							commentNumLikes++;
							spanNumCommentLikes.textContent = commentNumLikes;
						})
						.catch((error) => {
							console.log(`에러 발생!!! ${error}`);
						})
				}
				
				
				
			});
			
		});
	}

	// 댓글 삭제버튼 이벤트리스너 함수
	const registerCommentDeleteEventListener = function() {
		const btnCommentDeleteList = document.querySelectorAll('.btn-post-comment-delete');
		
		btnCommentDeleteList.forEach((btnCommentDelete) => {
			
			const commentIdToDelete = btnCommentDelete.getAttribute('commentId');		
			
			btnCommentDelete.addEventListener('click', function() {
				
				const data = {
					comment: {
						commentId: commentIdToDelete
					}				
				}						
				
				axios.delete(`/board/comment/delete/${commentIdToDelete}`, data)
					.then(() => {
						alert('댓글 삭제 성공!');
						refreshComments();
					})
					.catch((error) => {
						console.log(`에러 발생!!! ${error}`);
					})			
			})
			
		});		
	}
	
	
	// 답글 버튼누르면 textarea나오도록 하는 이벤트리스너 등록 함수
	const registerBtnCommentReplyEventListener = function() {
		const btnCommentReplyList = document.querySelectorAll('.btn-post-comment-reply');
		
		btnCommentReplyList.forEach((btnCommentReply) => {
			
			const postCommentReplyContainer = btnCommentReply.closest('.post-comment-reply-container');
			const commentAddReplyContainer = postCommentReplyContainer.nextElementSibling;
			
			btnCommentReply.addEventListener('click', function() {
				console.log(btnCommentReply);
				console.log(commentAddReplyContainer);
				if(commentAddReplyContainer.classList.contains('d-none')) {
					commentAddReplyContainer.classList.remove('d-none');
				} else {
					commentAddReplyContainer.classList.add('d-none');
				}
				
			});		
		});		
	}
	
	
	// 답글 다는 이벤트리스너등록 함수
	const registerBtnAddReplyEventListener = function() {
		
		const btnAddReplyList = document.querySelectorAll('.btn-post-comment-add-reply');
		
		btnAddReplyList.forEach((btnAddReply) => {
			
			const addReplyContainer = btnAddReply.closest('.post-comment-add-reply-container');
			const textareaAddReply = addReplyContainer.querySelector('.textarea-post-comment-reply');
			
			const replyTo = btnAddReply.getAttribute('commentId');
			const authorNicknameReplyingTo = btnAddReply.getAttribute('author');
			
			btnAddReply.addEventListener('click', function() {
                
                if (!user) {
                    alert('로그인 한 유저만 댓글을 달 수 있습니다.');
                    return;
                }
                
				const data = {			
					member: {
						email: user.getAttribute('email')
					},
					post: {
						postId: postId
					},
					content: textareaAddReply.value,
					replyTo: replyTo,						
				}
				
				axios.post('/board/comment/add', data)
					.then(() => {
						refreshComments();
					})
					.catch((error) => {
						console.log(`Error 발생!!! ${error}`);
					})			
			});
			
		});	
	}
	
	
	// 대댓글 답변받는 사람 누르면 해당 답변받는 댓글로 이동시켜주는 이벤트리스너 등록 함수
	const registerMoveToRepliedCommentEventListener = function() {
		
		const spanRepliedAuthorList = document.querySelectorAll('.span-post-comment-replied-author');
		
		spanRepliedAuthorList.forEach((spanRepliedAuthor) => {
			
			const replyTo = spanRepliedAuthor.getAttribute('replyTo');
			const repliedCommentContainer = document.querySelector(`.post-comment-each-container[commentId="${replyTo}"]`);
			
			spanRepliedAuthor.addEventListener('click', function() {
				
				repliedCommentContainer.scrollIntoView({ behavior: 'smooth', block: 'center' });
				
				// 반짝반짝 효과줌
				repliedCommentContainer.classList.add('post-comment-replied');
				setTimeout(() => {
					repliedCommentContainer.classList.remove('post-comment-replied');
				}, 1000);
				
			});
			
		});
		
	}
	
	
	// 댓글 refresh하는 함수
	const refreshComments = function() {
		
		const params = {
			postId: postId
		};
		
		axios.get('/board/comment/refresh', {params})
			.then((response) => {
				let htmlContent = '';
				commentContainer.innerHTML = '';
				
				const commentDtoList = response.data;
				for (let comment of commentDtoList) {
					
					
					htmlContent += `
			            <!-- 각 댓글에 대한 부분 -->
			            <div>
			                <div class="post-comment-each-container" commentId="${comment.commentId}">           
			                    <div class="post-comment-each-header">
			                        <div class="post-comment-author-create-date-container post-comment-text-vertical-center">
                    `;
                    
                    if (comment.member.usersaveprofile.toLowerCase().startsWith('http')) {
						htmlContent += `
			                            <div class="post-comment-each-profile-container">                                                                
			                                <img class="post-comment-each-profile" src="${comment.member.usersaveprofile}"  id="profile_s_img" alt="User Profile">
										</div>							
						`;
					} else if (!comment.member.usersaveprofile.toLowerCase().startsWith('http') && comment.member.usersaveprofile == 'userimage.png') {
						htmlContent += `
			                            <div class="post-comment-each-profile-container">                                                                
			                                <img class="post-comment-each-profile" src="/image/userimage.png" alt="User Profile">
										</div>
						`;
					} else {
						htmlContent += `
			                            <div class="post-comment-each-profile-container">                                                                
			                                <img class="post-comment-each-profile" src="/image?photo=${comment.member.usersaveprofile}" alt="User Profile">                                
										</div>
						`;						
					}
					
					
					
					htmlContent += `
			                            <div class="post-comment-each-author">${comment.member.nickname}</div>
			                            ${comment.member.nickname === authorNickname ? '<div class="post-comment-by-post-author post-comment-text-vertical-center">작성자</div>' : ''}
			                        </div>    <!-- post-comment-author-create-date-container 끝 -->`;
			                        
					if(comment.timeDifferenceInMinute != null || comment.timeDifferenceInMinute == 0) {	// 만약 24시간 내에 작성된 댓글일 경우
						if (comment.timeDifferenceInMinute < 60) {	// 1시간 이내 작성된 댓글인 경우
							htmlContent += `
								<div class="post-comment-each-created-time post-comment-text-vertical-center">${comment.timeDifferenceInMinute}분 전</div>
							`;
						} else {	// 1시간 초과된 댓글인 경우
							htmlContent += `
								<div class="post-comment-each-created-time post-comment-text-vertical-center">${Math.floor(comment.timeDifferenceInMinute / 60)}시간 전</div>
							`;
						}
					} else {	// 작성 후 24시간 경화한 댓글인 경우
						htmlContent += `
							    <div class="post-comment-each-created-time post-comment-text-vertical-center">${formatTime(comment.createdTime)}</div>
						`;
					}			                        
			                        
					htmlContent += `			                        
			                    </div>  <!-- post-comment-each-header 끝 -->
			                    <div class="post-comment-body-container">
			                        <div class="post-comment-content">${comment.content}</div>`;
                    if(comment.isDeleted == null) { // 만약 삭제된 댓글일 경우 이부분 div.post-comment-each-like-container 제외
                        
			        htmlContent +=` 
			                     <div class="post-comment-each-like-container">
		                            <div>
		                                <span>likes</span>
		                                <span class="span-num-comment-likes">${comment.commentLikesList.length}</span>
		                            </div>
                    `;
                    
                        if (user && comment.member.nickname == user.getAttribute('nickname')) {
						    htmlContent += `
	                                <div></div>
	                                <div>
	                                    <button class="btn-post-comment-delete" commentId="${comment.commentId}" author="${comment.member.nickname}"><i class="fa-solid fa-trash"></i></button>
	                                </div>
                        `;
					    } else {
						      htmlContent += `
	                                <div class="post-comment-each-like-report-icon-container">
	                                    <div class="post-comment-like-container
	                                    	 ${user &&comment.commentLikesList.some((element) => element.member.email === user.getAttribute('email')) ? 'post-comment-already-liked' : ''}" 
											 commentId="${comment.commentId}" author="${comment.member.nickname}">
	                                        <i class="fa-solid fa-thumbs-up"></i>
	                                    </div>
	                                </div>
                        `;						
					   }
					
				
			            htmlContent +=`
		                        </div> <!-- post-comment-each-like-container 끝 -->
			            `;
                    }   // if문 comment.isDeleted == null 끝!!
                    htmlContent +=`
			                        <div class="post-comment-reply-container">
			        `;
			        
		            if (comment.isDeleted == null) {                            
                        htmlContent += `
		                            <button class="btn-post-comment-reply">답글</button>
                        `;                    
                    }
                    
                    htmlContent += `
		                        </div>
                    `;
                    
                    if(comment.isDeleted == null) {
                        htmlContent += `
                                <div class="d-none post-comment-add-reply-container">
                                    <textarea class="textarea-post-comment-reply"></textarea>
                                    <button class="btn-post-comment-add-reply" commentId="${comment.commentId}" author="${comment.member.nickname}">댓글 등록</button>
                                </div>                            
                        `;
                    }
                    
                    htmlContent += `
		                    </div>  <!-- comment body 끝 -->
		                </div> <!-- post-comment-each-container 끝 -->
				    `;
					
					if (!comment.repliesList) {	// 해당댓글에 대댓글이 없는 경우
						htmlContent += `
							</div>
						`;						
					} else {	// 원댓글에 대댓글이 있는 경우
						for (let replyComment of comment.repliesList) {	// 대댓글들을 loop시킴		
							
							htmlContent += `						
				                <!-- 대댓글 부분 -->
				                <div class="post-comment-each-container post-comment-each-reply-container" commentId="${replyComment.commentId}">
				                    <div class="post-comment-each-header">
				                        <div class="post-comment-reply-author-create-date-container post-comment-text-vertical-center">
				                            <div class="post-comment-reply-l-icon-container post-comment-text-vertical-center">
				                                <i class="fa-solid fa-l"></i>
				                            </div>
                            `;
                            
                            if (replyComment.member.usersaveprofile.toLowerCase().startsWith('http')) {
								htmlContent += `
				                            <div class="post-comment-each-profile-container">
				                                <img class="post-comment-each-profile" src="${replyComment.member.usersaveprofile}"  id="profile_s_img" alt="User Profile">				                                
				                            </div>								
								`;
							} else if (!replyComment.member.usersaveprofile.toLowerCase().startsWith('http') && replyComment.member.usersaveprofile == 'userimage.png') {
								htmlContent += `
				                            <div class="post-comment-each-profile-container">
				                                <img class="post-comment-each-profile" src="/image/userimage.png" alt="User Profile">				                                
				                            </div>
								`;								
							} else {
								htmlContent += `
				                            <div class="post-comment-each-profile-container">
				                                <img class="post-comment-each-profile" src="/image?photo=${replyComment.member.usersaveprofile}" alt="User Profile">				                                
				                            </div>
								`;										
							}
							
							htmlContent += `
				                            <div class="post-comment-each-author post-comment-text-vertical-center">${replyComment.member.nickname}</div>
				                            ${replyComment.member.nickname == authorNickname ? '<div class="post-comment-by-post-author post-comment-text-vertical-center">작성자</div>' : ''}
				                        </div>   <!-- post-comment-reply-author-create-date-container 끝 -->`;
				            if (replyComment.timeDifferenceInMinute != null || replyComment.timeDifferenceInMinute == 0) {
								if (replyComment.timeDifferenceInMinute < 60) {
									htmlContent += `
										<div class="post-comment-each-created-time post-comment-text-vertical-center">${replyComment.timeDifferenceInMinute}분 전</div>
									`;
								} else {
									htmlContent += `
										<div class="post-comment-each-created-time post-comment-text-vertical-center">${Math.floor(replyComment.timeDifferenceInMinute / 60)}시간 전</div>
									`;
								}
							} else {
								htmlContent += `
									<div class="post-comment-each-created-time post-comment-text-vertical-center">${formatTime(replyComment.createdTime)}</div>
								`;
							}
				                        
				                        
				            htmlContent += `           
				                    </div> <!-- post-comment-each-header 끝 -->
				                    <div class="post-comment-body-container post-comment-reply-body-container">
				                        <div class="post-comment-content">
	                        `;
	                        
	                        if(replyComment.isDeleted == null) {   // 삭제된 댓글이면 답변받는 사람 닉네임 가리기
                                htmlContent += `                                
				                            <span class="span-post-comment-replied-author" replyTo="${replyComment.replyTo}">${replyComment.commentReplied.member.nickname}</span>
                                `;
                            }
                            
                            htmlContent += `                            
				                            <span class="span-post-comment-content">${replyComment.content}</span>
				                        </div>
	                        `;
	                        
                           if(replyComment.isDeleted == null) {
                               htmlContent += `    
				                        <div class="post-comment-each-like-container">
				                            <div>
				                                <span>likes</span>
				                            	<span class="span-num-comment-likes">${replyComment.commentLikesList.length}</span>
				                            </div>
							        `;
							
							    if (user && replyComment.member.nickname == user.getAttribute('nickname')) {	// 로그인유저가 대댓글 작성했을 경우
								    htmlContent += `
			                                <div></div>
			                                <div>
			                                    <button class="btn-post-comment-delete" commentId="${replyComment.commentId}" author="${replyComment.member.nickname}"><i class="fa-solid fa-trash"></i></button>
			                                </div>   																
								    `;
							    } else {	// 그 외의 경우
								    htmlContent += `
			                                <div class="post-comment-each-like-report-icon-container">
			                                    <div class="post-comment-like-container
			                                    	 ${user &&replyComment.commentLikesList.some((element) => element.member.email === user.getAttribute('email')) ? 'post-comment-already-liked' : ''}"
			                                    	commentId="${replyComment.commentId}" author="${replyComment.member.nickname}">
			                                        <i class="fa-solid fa-thumbs-up"></i>
			                                    </div>
			                                </div>  
								    `;
						    	}
						    	
							    htmlContent += `
				                        </div> <!-- post-comment-each-like-container 끝 -->
		                        `;
                            }
                            

                            htmlContent += `
                                        <div class="post-comment-reply-container">
                            `;
                             
                             if(replyComment.isDeleted == null) {
                                 htmlContent += `
                                                <button class="btn-post-comment-reply">답글</button>                                                     
                                 `;                                 
                             }
                             
                             htmlContent += `
                                        </div>                             
                             `;                           
                             
                            if(replyComment.isDeleted == null) {
                               htmlContent += ` 
                                        <div class="d-none post-comment-add-reply-container">
                                            <textarea class="textarea-post-comment-reply"></textarea>
                                            <button class="btn-post-comment-add-reply" commentId="${replyComment.commentId}" author="${replyComment.member.nickname}">댓글 등록</button>
                                        </div>
                                `;                                
                            }

	                        htmlContent += `
				                    </div>
				                </div> <!-- each 대댓글 컨테이너 끝 -->
				            </div> <!-- 가장 큰 div 끝 -->							
							`;
						}	// 대댓글 for문 끝					
					}	// 대댓글 있는 경우인 else끝	
					
				}	// for문 끝
				
				commentContainer.innerHTML = htmlContent;
				
				registerCommentLikeEventListener();
				registerCommentDeleteEventListener();
				registerBtnCommentReplyEventListener();
				registerBtnAddReplyEventListener();
				registerMoveToRepliedCommentEventListener();
				
				axios.get(`/board/${postId}/num-of-comments`)
					.then((response) => {
						spanNumCommentsList.forEach((spanNumComments) => spanNumComments.textContent = response.data);
					})
					.catch((error) => {
						console.log(`에러발생!!! ${error}`);
					})
				
			})
			.catch((error) => {
				console.log(`에러 발생!!! ${error}`);
			});
			
		
	}
	
	
	
	// 시간 포매팅하는 함수
	const formatTime = function(timeToFormat) {
		
		  const date = new Date(timeToFormat);
		  
		  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`;	
	}
	
	
	
	
	
	
	
	// 좋아요 버튼 누를 때 이벤트 리스너
	btnPostLike.addEventListener('click', function() {

		if(!user) {
			alert('로그인 한 유저만 좋아요를 누를 수 있습니다.');
			return;
		}
		
		if (user.getAttribute('nickname') === authorNickname) {
			alert('내 게시글엔 좋아요를 누를 수 없습니다.');
			return;
		}
		
		const data = {
			member: {
				email: user.getAttribute('email')
			},
			post: {
				postId: postId
			}			
		}
		
		if(!btnPostLike.classList.contains('liked-already')) {	// 좋아요 누르지 않은 상태인 경우
			axios.post('/board/like', data)
				.then((response) => {				
					numLikes ++;
					spanNumLikes.textContent =  numLikes;
					btnPostLike.classList.add('liked-already');
					spanLikeBtn.textContent = '좋아요 취소';
					
				})
				.catch((error) => {
					console.log(`에러발생!!! ${error}`);	
				});			
		} else {	// 좋아요 이미 누른경우
			axios.post('/board/like/delete', data)
				.then((response) => {
					numLikes--;
					spanNumLikes.textContent = numLikes;
					btnPostLike.classList.remove('liked-already');
					spanLikeBtn.textContent = '좋아요';
					console.log(numLikes);
				})
				.catch((error) => {
					console.log(`에러발생!!! ${error}`);
				})
		}

	});
	
	
	// 댓글다는 버튼 이벤트 리스너
	btnAddPostComment.addEventListener('click', function() {
		
		if(!user) {
			alert('댓글을 달기 위해서는 로그인해주세요.');
			return;
		}
		
		if (!textareaPostComment.value) {
			alert('댓글 내용을 입력해주세요');
			return;
		}
		
		const userProfileImage = user.querySelector('img');
		console.log(userProfileImage);
		
		const data = {
			member: {
				email: user.getAttribute('email')
			},
			post: {
				postId: postId
			},
			content: textareaPostComment.value			
		}
		
		axios.post('/board/comment/add', data)
			.then((response) => {
				alert('댓글 등록 성공');
				textareaPostComment.value = '';
				
				refreshComments();
				
			})
			.catch((error) => {
				console.log(`에러 발생!!! ${error}`)
			})
		
	});
	
	
	btnCommentRefresh.addEventListener('click', refreshComments);
	

	
	
	// 댓글 좋아요, 삭제 등 이벤트리스너 등록하는 함수 실행
	registerCommentLikeEventListener();
	registerCommentDeleteEventListener();
	registerBtnCommentReplyEventListener();
	registerBtnAddReplyEventListener();
	registerMoveToRepliedCommentEventListener();
	
	
});