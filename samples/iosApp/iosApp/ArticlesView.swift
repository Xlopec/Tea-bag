import SwiftUI
import SharedAppLib
import Kingfisher
import CoreMIDI
import CoreAudio

struct ArticlesView: View {
    
    let state: ArticlesState
    let handler: MessageHandler
    @State private var searchHintText: String
    @State private var tab: Int = 0
    @State private var searchText: String
    private let headingText: String
    
    init(state: ArticlesState, handler: @escaping MessageHandler, searchHintText: String, headingText: String) {
        self.state = state
        self.handler = handler
        self.searchText = state.filter.query as? String ?? ""
        self.searchHintText = searchHintText
        self.headingText = headingText
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            
            Text(headingText)
                .font(.title)
                .padding()
            
            SearchBar(text: $searchText, hintText: searchHintText) {
                handler(LoadArticles(id: state.id))
            }.onChange(of: searchText) { updatedSearchText in
                // todo send request to fetch and show suggestions
                handler(FilterUpdated(filter: Filter(type: state.filter.type, query: updatedSearchText, sources: [])))
            }
            
            let data = state.loadable.data as? Array<Article> ?? []
            
            if state.loadable.isLoading {
                ZStack {
                    ProgressView()
                }
                .frame(
                    maxWidth: .infinity,
                    maxHeight: .infinity,
                    alignment: .center
                )
            } else if let transientState = state.loadable.loadableState as? Exception, data.isEmpty {
                MessageView(message: transientState.displayMessage, actionButtonMessage: "Retry") {
                    handler(RefreshArticles(id: state.id))
                }.frame(
                    maxWidth: .infinity,
                    maxHeight: .infinity,
                    alignment: .center
                )
            } else if data.isEmpty {
                MessageView(message: "No articles found", actionButtonMessage: "Reload") {
                    handler(RefreshArticles(id: state.id))
                }.frame(
                    maxWidth: .infinity,
                    maxHeight: .infinity,
                    alignment: .center
                )
            } else {
                List {
                    
                    ForEach(data, id: \.url) { article in
                        RowItem(screenId: state.id, article: article, handler: handler)
                            .onAppear {
                                // give window of size of 2 last items in order to prefetch next articles
                                // before user will scroll to the end of the list
                                if article == data[safe: data.count - 2] {
                                    handler(LoadNextArticles(id: state.id))
                                }
                            }.onTapGesture {
                                handler(NavigateToArticleDetails(article: article, id: state.id))
                            }
                    }
                    
                    VStack(alignment: .center) {
                        if state.loadable.isLoadingNext {
                            ProgressView()
                        } else if let transientState = state.loadable.loadableState as? Exception {
                            MessageView(message: transientState.displayMessage, actionButtonMessage: "Retry") {
                                handler(RefreshArticles(id: state.id))
                            }
                        }
                    }.frame(maxWidth: .infinity)
                }.refreshable {
                    handler(RefreshArticles(id: state.id))
                }
            }
        }
    }
}

struct RowItem: View {
    
    private static let dateFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.dateFormat = "dd MMM' at 'hh:mm"
        return formatter
    }()
    
    let article: Article
    let handler: MessageHandler
    let screenId: UUID
    
    // todo how to pass environmental vars implicitly?
    init(screenId: UUID, article: Article, handler: @escaping MessageHandler) {
        self.article = article
        self.handler = handler
        self.screenId = screenId
    }
    
    var body: some View {
        
        VStack(alignment: .leading, spacing: 10, content: {
            
            if let image = article.urlToImage {
                KFImage.url(image)
                    .resizable()
                    .fade(duration: 0.25)
                    .aspectRatio(contentMode: .fit)
                    .frame(height: 200, alignment: .center)
            }
            // todo get rid of casts
            Text(article.title as! String)
            
            if let author = article.author as? String {
                Text("By \(author)")
                    .font(.caption)
            }
            
            if let description = article.description_ as? String {
                Text(description)
                    .font(.subheadline)
                    .lineLimit(100)
            }
            
            Text("Published on \(RowItem.dateFormatter.string(from: article.published))")
                .font(.caption)
        }).background {
            // Context menu seems keeping old state and doesn't update on state change
            // see: https://stackoverflow.com/questions/68313742/why-does-context-menu-display-the-old-state-even-though-the-list-has-correctly-b
            Color
                .clear
                .contextMenu {
                    Button {
                        handler(OnShareArticle(article: article))
                    } label: {
                        Label("Share", systemImage: "square.and.arrow.up")
                    }
                    
                    Button {
                        handler(ToggleArticleIsFavorite(id: screenId, article: article))
                    } label: {
                        Label(article.isFavorite ? "Remove from favorite" : "Add to favorite", systemImage: article.isFavorite ? "heart.fill" : "heart")
                    }
                }.id(article.isFavorite)
        }
        
        /*.padding(
         EdgeInsets(
         top: CGFloat(16.0),
         leading: CGFloat(16.0),
         bottom: CGFloat(16.0),
         trailing: CGFloat(16.0)
         )
         )*/
    }
}

/*struct ContentView_Previews: PreviewProvider {
    
    static private let filter = Filter(type: FilterType.favorite, query: "", sources: [])
    static private let article = Article(url: URL(string: "www.google.com")!, title: "Title", author: nil, description: nil, urlToImage: nil, published: Date(), isFavorite: true, source: nil)
    static private let loadable = Loadable<AnyObject>.companion.doNewLoading(data: [article])
    
    static private let state = ArticlesState(id: UUID(), filter: filter, loadable: loadable, scrollState: ScrollState.companion.Initial)
    
    static var previews: some View {
        RowItem(screenId: UUID(), article: article) {_ in }
        
        ArticlesView(state: state, handler: { _ in }, searchHintText: "Search in articles", headingText: "Feed")
    }
}*/

private extension Exception {
    var displayMessage: String { return th.message }
}

private extension Collection {
    
    subscript (safe index: Index) -> Element? {
        return indices.contains(index) ? self[index] : nil
    }
    
}
